package main;

import java.io.IOException;
import java.net.URL;
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

  @Override
  protected void aggregatePricesAndPromos() {
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
                  requestedUrl.getFile().contains("?") ? requestedUrl.getFile()
                      .indexOf("?") : requestedUrl.getFile().length());
            }
          };

      HtmlPage page = webClient.getPage(URL_SHUFERSAL);

      // Current page:
      // Title=×§×•×�×•×¤ ×™×©×¨×�×œ - Coop Israel
      // URL=http://coopisrael.coop/home/prices

      if (aggType.equals(AggregatorType.daily)) {
        page = (HtmlPage) selectCategory(page, "PricesFull");
        saveTableRows(webClient, page);

        page = (HtmlPage) selectCategory(page, "PromosFull");
        saveTableRows(webClient, page);

        page = (HtmlPage) selectCategory(page, "Stores");
        saveTableRows(webClient, page);
      } else if (aggType.equals(AggregatorType.hourly)) {
        page = (HtmlPage) selectCategory(page, "Prices");
        saveTableRows(webClient, page);

        page = (HtmlPage) selectCategory(page, "Promos");
        saveTableRows(webClient, page);
      } else
        throw new RuntimeException("Not supported aggType: " + aggType);

    } catch (Exception e) {
    	logSevere(e, "aggregatePricesAndPromos");
    } finally {
      webClient.close();
    }
  }

  private void saveTableRows(AggregatorWebClient webClient, HtmlPage page)
      throws IOException {
    HtmlAnchor nextTablePage = null;
    boolean goHead;
    do {
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
                (HtmlAnchor) ((HtmlTableDataCell) row
                    .getElementsByTagName("td").get(0)).getElementsByTagName(
                    "a").get(0);
            HtmlTableCell fileNameCell = row.getCells().get(6);
            if (fileValidator.shouldAggregateFile(fileNameCell.getTextContent()
                .trim())) {
              boolean success = false;
              int trialsCnt = 0;
              int maxTrials = 5;
              while (!success && trialsCnt < maxTrials) {
                try {
                  a.click();
                  success = true;
                } catch (FailingHttpStatusCodeException e) {
                  trialsCnt++;
                  logger.warning("Exception was thrown while requesting: "
                      + a.getAttribute("href") + "\nStatus code: "
                      + e.getStatusCode() + ", status message: "
                      + e.getStatusMessage() + ". Attempting for maximum "
                      + maxTrials + " times.");
                } catch (Exception e1) {
                  trialsCnt++;
                  logger.warning("Exception was thrown while requesting: "
                      + a.getAttribute("href") + "\n " + e1.getClass() + ": "
                      + e1.getMessage() + "\nMessage: " + e1.getMessage()
                      + ". Attempting for maximum " + maxTrials + " times.");
                }
              }
              if (!success) {
                logger.severe("Failed to request " + a.getAttribute("href")
                    + ". Attempted the maximal " + maxTrials + " times.");
              }
            }
          }
        } catch (Exception e) {
        	logSevere(e, "saveTableRows");
        }
      }

      DomNodeList<HtmlElement> tfootList = table.getElementsByTagName("tfoot");
      if (tfootList.size() > 0) {
        HtmlTableFooter tableFoot =
            (HtmlTableFooter) table.getElementsByTagName("tfoot").get(0);
        DomNodeList<HtmlElement> anchors = tableFoot.getElementsByTagName("a");
        for (HtmlElement a : anchors) {
          if (a.getTextContent().trim().equals(">")) {
            nextTablePage = (HtmlAnchor) a;
          }
        }
        if (nextTablePage != null) {
          page = nextTablePage.click();
          goHead = true;
        }
      }

    } while (goHead);
  }

  private Page selectCategory(HtmlPage page, String categoryText) {
    HtmlSelect select1;
    try {
      select1 =
          (HtmlSelect) page.getElementsByIdAndOrName("ddlCategory").get(0);
    } catch (Exception e) {
      throw new RuntimeException("Did not fine ddlCategory element.", e);
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
            .append(
                "In Hourly mode: foreach Category selection: Promos and Prices, iterates the table's rows and aggregates files of the current day and which have not yet been ")
            .append("aggregated earlier today.")
            .append(lineSeperator)
            .append(
                "Time stamp and branches are parsed from the valud in the \"Name\" column.")
            .append(lineSeperator)
            .append(lineSeperator)
            .append(
                "Warning and Errors are logged to err.log at: "
                    + this.sessionDir);

    return sb.toString();
  }

}
