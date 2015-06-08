package main;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLFormElement;

public class CoopAggregator extends Aggregator {

  protected static final String URL_PRICES =
      "http://coopisrael.coop/home/prices";
  private static final String URL_PROMOS = "http://coopisrael.coop/home/promos";
  protected static final String URL_STORES =
      "http://coopisrael.coop/home/branches";

  private int branchesCnt_prices = 0;
  private Set<String> branchesFailed_prices = new HashSet<String>();

  private int branchesCnt_promos = 0;
  private Set<String> branchesFailed_promos = new HashSet<String>();

  private int storesCnt = 0;

  @Override
  protected void aggregatePricesAndPromos() {
    // running the prices-aggregator
    Thread priceThread = new Thread(getPriceAggregator());
    priceThread.start();

    // running the promos-aggregator
    Thread promosThread = new Thread(getPromoAggregator());
    promosThread.start();

    // running the promos-aggregator
    Thread storesThread = new Thread(getStoresAggregator());
    storesThread.start();

    try {
      priceThread.join();
      promosThread.join();
      storesThread.join();
    } catch (InterruptedException e) {
      //e.printStackTrace();
    }
  }

  private Runnable getStoresAggregator() {
    return new Runnable() {

      @Override
      public void run() {
        try {
          String path = sessionDir;

          AggregatorWebClient webClient =
              new AggregatorWebClient(BrowserVersion.FIREFOX_31, path, aggType,
                  logger);

          HtmlPage page = webClient.getPage(URL_STORES);

          logger
              .info("Aggregating prices in page: ×§×•×�×•×¤ ×™×©×¨×�×œ - Coop Israel");

          // Current page:
          // Title=×§×•×�×•×¤ ×™×©×¨×�×œ - Coop Israel
          // Title=׳§׳•׳�׳•׳₪ ׳™׳©׳¨׳�׳� - Coop Israel
          // Title=׳§׳•׳�׳•׳₪ ׳™׳©׳¨׳�׳� - Coop Israel
          // URL=http://coopisrael.coop/home/prices

          selectXmlType(page);

          checkAgreeBox(page);

          HtmlForm form = ((HtmlForm) page.getElementById("branches"));
          HTMLFormElement HTMLform = (HTMLFormElement) form.getScriptObject();

          HtmlElement submitButton = (HtmlElement) form.getLastElementChild();

          int attempCnt = 0;
          boolean success = false;
          while (!success) {
            try {
              // don't know why, but needed to submit HTMLform before clicking
              // the submitButton itself...
              HTMLform.submit();
              webClient.waitForBackgroundJavaScript(0);
              ((HTMLElement) submitButton.getScriptObject()).click();
              success = true;
            } catch (Exception e1) {
              attempCnt++;
              logger.info("Attempts #: " + attempCnt + "; (Exception: "
                  + e1.getClass() + "\t Message: " + e1.getMessage() + ")");
              if (attempCnt <= 5)
                logger.info("Trying again...");
              else {
                logger
                    .severe("Quit attempts to download stores-files after 5 times"
                        + "\n Exception thrown:" + e1.getMessage() + Aggregator.stacktraceToString(e1));
                break;
              }
            }
          }
          if (success)
            storesCnt++;

          logger.info("Finished aggregating Coop stores.");

          webClient.close();

        } catch (Exception e) {
          logger.severe(e.getClass().toString() + " thrown:" + e.getMessage() + Aggregator.stacktraceToString(e));
          
        }
      }

    };

  }

  private Runnable getPriceAggregator() {

    return new Runnable() {

      @Override
      public void run() {
        try {
          String path = sessionDir;

          AggregatorWebClient webClient =
              new AggregatorWebClient(BrowserVersion.FIREFOX_31, path, aggType,
                  logger);

          HtmlPage page = webClient.getPage(URL_PRICES);

          logger
              .info("Aggregating prices in page: ×§×•×�×•×¤ ×™×©×¨×�×œ - Coop Israel");


          // Current page:
          // Title=׳§׳•׳�׳•׳₪ ׳™׳©׳¨׳�׳� - Coop Israel
          // URL=http://coopisrael.coop/home/prices

          selectXmlType(page);

          checkAgreeBox(page);

          Iterable<DomElement> branches = getAllBranches(webClient, page);

          HtmlForm form = ((HtmlForm) page.getElementById("pricesForm"));
          HTMLFormElement HTMLform = (HTMLFormElement) form.getScriptObject();

          for (DomElement branch : branches) {
            if (submitBranch(webClient, page, form, HTMLform, branch))
              branchesCnt_prices++;
            else
              branchesFailed_prices.add(branch.getAttribute("data-id"));
          }

          logger.info("Finished aggregating Coop prices.");

          webClient.close();

        } catch (Exception e) {
            logger.severe(e.getClass().toString() +  " thrown:" + e.getMessage() + Aggregator.stacktraceToString(e));
        }
      }

    };

  }

  private Runnable getPromoAggregator() {
    return new Runnable() {

      @Override
      public void run() {
        try {
          String path = sessionDir;

          AggregatorWebClient webClient =
              new AggregatorWebClient(BrowserVersion.FIREFOX_31, path, aggType,
                  logger);

          HtmlPage page = webClient.getPage(URL_PROMOS);

          selectXmlType(page);

          checkAgreeBox(page);

          Iterable<DomElement> branches = getAllBranches(webClient, page);

          HtmlForm form = ((HtmlForm) page.getElementById("promoForm"));
          HTMLFormElement HTMLform = (HTMLFormElement) form.getScriptObject();

          for (DomElement branch : branches) {
            if (submitBranch(webClient, page, form, HTMLform, branch))
              branchesCnt_promos++;
            else
              branchesFailed_promos.add(branch.getAttribute("data-id"));
          }

          logger.info("Finished aggregating Coop promos.");

          webClient.close();

        } catch (Exception e) {
          logger.severe(e.getClass().toString() +  " thrown:" + e.getMessage() + Aggregator.stacktraceToString(e));
        }
      }

    };
  }

  protected boolean submitBranch(AggregatorWebClient webClient, HtmlPage page,
      HtmlForm form, HTMLFormElement HTMLform, DomElement branch) {

    // the javascript "setBranch(this)" threw runtime exception, so we
    // put the branch manually
    // ((HTMLElement) branch.getScriptObject()).click();
    ((HtmlHiddenInput) page.getElementByName("branch"))
        .setValueAttribute(branch.getAttribute("data-id"));

    HtmlElement submitButton = (HtmlElement) form.getLastElementChild();

    int attempCnt = 0;
    boolean success = false;
    while (!success) {
      try {
        // don't know why, but needed to submit HTMLform before clicking
        // the
        // submitButton itself...
        HTMLform.submit();
        webClient.waitForBackgroundJavaScript(0);
        ((HTMLElement) submitButton.getScriptObject()).click();
        success = true;
      } catch (Exception e1) {// FailingHttpStatusCodeException e1) {
        attempCnt++;
        logger.info("Attempts #: " + attempCnt + "; (Exception: "
            + e1.getClass() + "\t Message: " + e1.getMessage() + ")");
        if (attempCnt <= 5)
          logger.info("Trying again...");
        else {
          logger.severe("Quit attempts after more than 5 times, for branch: "
              + branch.getTextContent() + ", data-id: "
              + branch.getAttribute("data-id") + "\n Exception thrown:"
              + e1.getMessage() + Aggregator.stacktraceToString(e1));
          
          break;
        }
      }
    }

    return success;
  }

  protected Iterable<DomElement> getAllBranches(WebClient webClient,
      HtmlPage page) throws IOException {
    HtmlSpan spanGetAllBranches =
        (HtmlSpan) (((HtmlInput) page.getElementById("branch_name")))
            .getNextSibling();
    ((HtmlElement) spanGetAllBranches).click();
    int js = webClient.waitForBackgroundJavaScript(0);
    while (js > 0) {
      js = webClient.waitForBackgroundJavaScript(1000);
    }

    HtmlDivision branchesDiv =
        (HtmlDivision) page.getElementById("match_branch");

    return branchesDiv.getChildElements();
  }

  protected void checkAgreeBox(HtmlPage page) throws IOException {
    HtmlInput checkbox3 = (HtmlInput) page.getElementByName("agree");
    checkbox3.click();
  }

  protected void selectXmlType(HtmlPage page) throws IOException {
    List<DomElement> radioButtons1 = page.getElementsByIdAndOrName("type");
    HtmlInput radioButton2 = null;
    for (DomElement element : radioButtons1) {
      HtmlInput inputElement = (HtmlInput) element;
      if ("1".equals(inputElement.getValueAttribute())) {
        radioButton2 = inputElement;
      }
    }
    radioButton2.click();
  }

  @Override
  protected String getReadmeDescription() {
    String lineSeperator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Aggregated prices from page : ")
            .append(URL_PRICES)
            .append(lineSeperator)
            .append("and promos from page: ")
            .append(URL_PROMOS)
            .append(lineSeperator)
            .append(
                "\"Daily\" and \"Hourly\" aggregators are identical, both aggregate xmls generated upon submitting a form.")
            .append(lineSeperator)
            .append(
                "\"Daily\" aggregators aggregate also stores files from url: "
                    + URL_STORES)
            .append("The xml files are saved in their original name. ")
            .append(lineSeperator).append("Prices: ").append(lineSeperator);

    if (branchesFailed_prices.isEmpty()) {
      sb.append("All prices downloaded successfully");
    } else {
      sb.append("ERROR! The following branches failed to download prices: ");
      for (String branchId : branchesFailed_prices) {
        sb.append(branchId).append("; ");
      }
    }
    sb.append(lineSeperator).append("A total of ").append(branchesCnt_prices)
        .append(" branches prices downloaded successfully")
        .append(lineSeperator);

    sb.append("Promos: ").append(lineSeperator);
    if (branchesFailed_promos.isEmpty()) {
      sb.append("All promos downloaded successfully");
    } else {
      sb.append("ERROR! The following branches failed to download promos: ");
      for (String branchId : branchesFailed_promos) {
        sb.append(branchId).append("; ");
      }
    }
    sb.append(lineSeperator).append("A total of ").append(branchesCnt_promos)
        .append(" branches promos downloaded successfully")
        .append(lineSeperator);

    if (aggType.equals(AggregatorType.daily)) {
      sb.append("Stores-files: ").append(storesCnt)
          .append(" files were saved.").append(lineSeperator);
    }

    sb.append(lineSeperator).append(
        "Warning and Errors are logged to err.log at: " + this.sessionDir);

    return sb.toString();
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.coop;
  }
}
