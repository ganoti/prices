package main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.management.RuntimeErrorException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableFooter;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

public class ShufersalAggregator extends Aggregator {

  private static final String URL_SHUFERSAL = "http://prices.shufersal.co.il/";
  private static final int MAX_TRIALS = 5;

  private int pricesCnt = 0;
  private Set<String> pricesFailed = new HashSet<String>();

  private int promosCnt = 0;
  private Set<String> promosFailed = new HashSet<String>();

  private int storesCnt = 0;

  @Override
  protected void aggregatePricesAndPromos() {
    try {
      // running the prices-aggregator
      Thread priceThread =
          new Thread(getAggregatorThread("PricesFull", "Prices"));
      priceThread.start();

      // running the promos-aggregator
      Thread promosThread =
          new Thread(getAggregatorThread("PromosFull", "Promos"));
      promosThread.start();

      // running the promos-aggregator
      Thread storesThread = new Thread(getAggregatorThread("Stores", null));
      storesThread.start();

      try {
        priceThread.join();
        promosThread.join();
        storesThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    } catch (Exception e) {
      logSevere(e, "aggregatePricesAndPromos");
    } finally {
      webClient.close();
    }
  }

  private Runnable getAggregatorThread(final String dailySelection,
      final String hourlySelection) {

    return new Runnable() {

      @Override
      public void run() {
        AggregatorWebClient webClient = null;
        try {
          String path = sessionDir;

          webClient =
              new AggregatorWebClient(BrowserVersion.FIREFOX_31, path, aggType,
                  logger) {
                private static final long serialVersionUID = 1L;

                @Override
                protected String getCompressedFileName(WebResponse response) {
                  URL requestedUrl = response.getWebRequest().getUrl();
                  return requestedUrl.getFile().substring(
                      requestedUrl.getFile().lastIndexOf("/") + 1,
                      requestedUrl.getFile().contains("?") ? requestedUrl
                          .getFile().indexOf("?") : requestedUrl.getFile()
                          .length());
                }
              };

          HtmlPage page = webClient.getPage(URL_SHUFERSAL);

          boolean isPrice = dailySelection.startsWith("Price");
          boolean isPromo = dailySelection.startsWith("Promo");
          boolean isStore = dailySelection.startsWith("Store");
          if (aggType.equals(AggregatorType.daily)) {
            page = attemptSelectingCategory(page, dailySelection);
            saveTableRows(webClient, page, isPrice, isPromo, isStore);
          } else if (aggType.equals(AggregatorType.hourly)
              && hourlySelection != null) {
            page = attemptSelectingCategory(page, hourlySelection);
            saveTableRows(webClient, page, isPrice, isPromo, isStore);
          }

          logger.info("Finished aggregating Shuf " + dailySelection + "; "
              + (hourlySelection != null ? hourlySelection : ""));

          webClient.close();

        } catch (MalformedURLException e1) {
          logger.severe("MalformedURLException thrown:" + e1.getMessage());
          e1.printStackTrace();
        } catch (IOException e1) {
          logger.severe("IOException thrown:" + e1.getMessage());
          e1.printStackTrace();
        } catch (Exception e) {
          logSevere(e, "saveTableRows");
        } finally {
          webClient.close();
        }
      }

      private void saveTableRows(AggregatorWebClient webClient, HtmlPage page,
          boolean isPrice, boolean isPromo, boolean isStore) throws IOException {
        HtmlAnchor nextTablePage;
        Integer pageNumber = 1;
        boolean goHead;
        do {
          nextTablePage = null;
          goHead = false;
          HtmlTable table =
              (HtmlTable) page.getElementById("gridContainer")
                  .getElementsByTagName("table").get(0);
          int js = webClient.waitForBackgroundJavaScript(0);
          while (js > 1) {
            js = webClient.waitForBackgroundJavaScript(1000);
            logger.info("Javascript processes: " + js + ". still waiting...");
          }

          String rowClass;
          for (HtmlTableRow row : table.getRows()) {
            try {
              rowClass = row.getAttribute("class");
              if (rowClass.equals("webgrid-row-style")
                  || rowClass.equals("webgrid-alternating-row")) {
                HtmlAnchor a =
                    (HtmlAnchor) ((HtmlTableDataCell) row.getElementsByTagName(
                        "td").get(0)).getElementsByTagName("a").get(0);
                HtmlTableCell fileNameCell = row.getCells().get(6);
                if (fileValidator.shouldAggregateFile(fileNameCell
                    .getTextContent().trim())) {
                  Object res = clickAndRepeatWhenFails(a);
                  if (res != null) {
                    if (isPrice)
                      pricesCnt++;
                    else if (isPromo)
                      promosCnt++;
                    else if (isStore)
                      storesCnt++;
                  }
                }
              }
            } catch (Exception e) {
              logger.severe("Exception while iterating Shufersal rows. "
                  + e.getMessage());
              e.printStackTrace();
            }
          }

          DomNodeList<HtmlElement> tfootList =
              table.getElementsByTagName("tfoot");
          if (tfootList.size() > 0) {
            HtmlTableFooter tableFoot =
                (HtmlTableFooter) table.getElementsByTagName("tfoot").get(0);
            DomNodeList<HtmlElement> anchors =
                tableFoot.getElementsByTagName("a");
            for (HtmlElement a : anchors) {
              // if (a.getTextContent().trim().equals(">")) {
              if (a.getTextContent().trim()
                  .equals(new Integer(pageNumber + 1).toString())) {
                nextTablePage = (HtmlAnchor) a;
                break;
              }
            }
            if (nextTablePage != null) {
              page = (HtmlPage) clickAndRepeatWhenFails(nextTablePage);
              pageNumber = pageNumber + 1;
              goHead = true;
            }
          }

        } while (goHead);
      }
    };
  }

  private Page clickAndRepeatWhenFails(HtmlAnchor a) {
    boolean success = false;
    int trialsCnt = 0;
    Page page = null;
    while (!success && trialsCnt < MAX_TRIALS) {
      try {
        page = a.click();
        success = true;
      } catch (FailingHttpStatusCodeException e) {
        trialsCnt++;
        logger.warning("Exception was thrown while requesting: "
            + a.getAttribute("href") + "\nStatus code: " + e.getStatusCode()
            + ", status message: " + e.getStatusMessage()
            + ". Attempting for maximum " + MAX_TRIALS + " times.");
      } catch (Exception e1) {
        trialsCnt++;
        logger.warning("Exception was thrown while requesting: "
            + a.getAttribute("href") + "\n " + e1.getClass() + ": "
            + e1.getMessage() + "\nMessage: " + e1.getMessage()
            + ". Attempting for maximum " + MAX_TRIALS + " times.");
      }
    }
    if (!success) {
      logger.severe("Failed to request " + a.getAttribute("href")
          + ". Attempted the maximal " + MAX_TRIALS + " times.");
    }
    return page;
  }

  private HtmlPage attemptSelectingCategory(HtmlPage page, String selection) {
    boolean success = false;
    int trialsCnt = 0;
    while (!success && trialsCnt < MAX_TRIALS) {
      try {
        page = (HtmlPage) selectCategory(page, selection);
        success = true;
      } catch (FailingHttpStatusCodeException e) {
        trialsCnt++;
        logger.warning("Exception was thrown while selecting " + selection
            + "\nStatus code: " + e.getStatusCode() + ", status message: "
            + e.getStatusMessage() + ". Attempting for maximum " + MAX_TRIALS
            + " times.");
      } catch (Exception e1) {
        trialsCnt++;
        logger.warning("Exception was thrown while selecting " + selection
            + "\n " + e1.getClass() + ": " + e1.getMessage() + "\nMessage: "
            + e1.getMessage() + ". Attempting for maximum " + MAX_TRIALS
            + " times.");
      }
    }
    if (!success) {
      logger.severe("Failed to select " + selection
          + ". Attempted the maximal " + MAX_TRIALS + " times.");
      throw new RuntimeErrorException(new Error("Failed selecting: "
          + selection));
    }
    return page;
  }

  private Page selectCategory(HtmlPage page, String categoryText) {
    HtmlSelect select1;
    try {
      select1 =
          (HtmlSelect) page.getElementsByIdAndOrName("ddlCategory").get(0);
    } catch (Exception e) {
      throw new RuntimeException("Did not find ddlCategory element.", e);
    }

    HtmlOption optionPricesFull = select1.getOptionByText(categoryText);

    return optionPricesFull.setSelected(true);
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.shuf;
  }

  @Override
  protected String getReadmeDescription() {
    String lineSeperator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Aggregated prices and promos from page : ")
            .append(URL_SHUFERSAL)
            .append(lineSeperator)
            .append(
                "In Daily mode: foreach Category selection: PromosFull, PricesFull and Stores, iterates the table's rows and aggregates files of the current day and which have not yet been ")
            .append("aggregated earlier today.")
            .append(lineSeperator)
            .append(
                "In Hourly mode: same as daily mode, but foreach Category selection: Promos and Prices.")
            .append(lineSeperator)
            .append(
                "Each category selection is performed on a different thread. ")
            .append(lineSeperator)
            .append(
                "Time stamp and branches are parsed from the value in the \"Name\" column.")
            .append(lineSeperator)
            .append(lineSeperator)
            .append("A total of ")
            .append(pricesCnt)
            .append(" prices files were downloaded successfully")
            .append(lineSeperator)
            .append("A total of ")
            .append(promosCnt)
            .append(" promos files were downloaded successfully")
            .append(lineSeperator)
            .append("A total of ")
            .append(storesCnt)
            .append(" stores files were downloaded successfully")
            .append(lineSeperator)
            .append(lineSeperator)
            .append(
                "Warning and Errors are logged to err.log at: "
                    + this.sessionDir);

    return sb.toString();
  }

}
