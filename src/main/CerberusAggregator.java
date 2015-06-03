package main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import main.PricesAggregatorApp.AggregatorType;

public abstract class CerberusAggregator extends Aggregator {

  private String URL_CERBERUS = "https://url.retail.publishedprices.co.il/";

  private int branchesCnt_prices = 0;
  private Set<String> branchesFailed_prices = new HashSet<String>();

  private int branchesCnt_promos = 0;
  private Set<String> branchesFailed_promos = new HashSet<String>();

  private int storesCnt = 0;

  protected abstract String getUsername();

  @Override
  protected void aggregatePricesAndPromos() {
    String path = sessionDir;

    AggregatorWebClient webClient =
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

    webClient.getOptions().setUseInsecureSSL(true);

    try {
      HtmlPage page = webClient.getPage(URL_CERBERUS);

      // Current page:
      // Title=Cerberus Web Client
      // URL=https://url.retail.publishedprices.co.il/login

      page = loginCerberus(page, getUsername());

      // Current page:
      // Title=Cerberus Web Client
      // URL=https://url.retail.publishedprices.co.il/file

      String rowId;
      HtmlTable filesTable = (HtmlTable) page.getElementById("fileList");

      int js = webClient.waitForBackgroundJavaScript(0);
      while (js > 1) {
        js = webClient.waitForBackgroundJavaScript(1000);
        logger.info("Javascript processes: " + js + ". still waiting...");
      }
      List<HtmlTableRow> rows = filesTable.getRows();
      logger.info("Found " + rows.size() + " rows in file-table. ");

      for (HtmlTableRow row : rows) {
        rowId = row.getId();
        if (rowId != null && !rowId.isEmpty()) {

          if (fileValidator.shouldAggregateFile(rowId)) {
            if (aggType.equals(AggregatorType.daily)) {
              // TODO count failures
              if (rowId.startsWith("PricesFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_prices++;
              } else if (rowId.startsWith("PromoFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_promos++;
              } else if (rowId.startsWith("Stores")) {
                downloadFromRow(row, webClient, page);
                storesCnt++;
              }
            } else if (aggType.equals(AggregatorType.hourly)) {
              if (rowId.startsWith("Prices") && !rowId.startsWith("PricesFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_prices++;
              } else if (rowId.startsWith("Promo")
                  && !rowId.startsWith("PromoFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_promos++;
              }
            } else
              throw new AggregatorException("Not supported aggType: " + aggType);
          }
        }
      }

    } catch (FailingHttpStatusCodeException e1) {
      logger.severe("FailingHttpStatusCodeException thrown:" + e1.getMessage());
      e1.printStackTrace();

    } catch (MalformedURLException e1) {
      logger.severe("MalformedURLException thrown:" + e1.getMessage());
      e1.printStackTrace();

    } catch (IOException e1) {
      logger.severe("IOException thrown:" + e1.getMessage());
      e1.printStackTrace();

    } catch (Exception e) {
      logger.severe("General exception thrown:" + e.getMessage());
      e.printStackTrace();
    }
  }

  private void downloadFromRow(HtmlTableRow row, WebClient webClient,
      HtmlPage page) throws FailingHttpStatusCodeException,
      MalformedURLException, IOException {

    HtmlAnchor a =
        (HtmlAnchor) ((HtmlTableDataCell) row.getElementsByTagName("td").get(0))
            .getElementsByTagName("a").get(0);

    a.click();
  }

  private HtmlPage loginCerberus(HtmlPage page, String username)
      throws IOException {
    HtmlTextInput textField1 = (HtmlTextInput) page.getElementById("username");
    textField1.setValueAttribute(username);

    HtmlElement theElement2 = (HtmlElement) page.getElementById("login-button");
    page = theElement2.click();
    return page;
  }

  @Override
  protected String getReadmeDescription() {
    String lineSeperator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Aggregated prices and promos from page : ")
            .append("https://url.retail.publishedprices.co.il/file")
            .append(lineSeperator)
            .append(
                "Iterates the rows of the fileList table, with a time-stamp of the current date.")
            .append(lineSeperator)
            .append(
                "Time stamp and branches are parsed from the row's id (which is the file name).")
            .append(lineSeperator)
            .append(
                "Xml files are extracted from the .gz files, and are saved in their original name.")
            .append(lineSeperator)
            .append(
                " The gz files are: all gz files that start with either \"PricesFull\" or \"PromoFull\" in Daily mode, or with either \"Prices\" or \"Promo\" (but do not continue with \"Full\"), in the hourly mode,")
            .append(lineSeperator)
            .append(
                "but have not yet been collected earlier in the same day (determined by the file names stored ealier today).")
            .append(lineSeperator).append(lineSeperator)
            .append("The following was downloaded: ").append(lineSeperator)
            .append("(1) Prices: ").append(lineSeperator);
    if (branchesFailed_prices.isEmpty()) {
      sb.append("All prices downloaded successfully");
    } else {
      sb.append("ERROR! The following branches failed to download prices: ");
      for (String branchId : branchesFailed_prices) {
        sb.append(branchId).append("; ");
      }
    }
    // TODO: specify how many prices for each branch
    sb.append(lineSeperator).append("A total of ").append(branchesCnt_prices)
        .append(" branches prices downloaded successfully")
        .append(lineSeperator);

    sb.append("(2) Promos: ").append(lineSeperator);
    if (branchesFailed_promos.isEmpty()) {
      sb.append("All promos downloaded successfully.");
    } else {
      sb.append("ERROR! The following branches failed to download promos: ");
      for (String branchId : branchesFailed_promos) {
        sb.append(branchId).append("; ");
      }
    }
    // TODO: specify how many promos for each branch
    sb.append(lineSeperator).append("A total of ").append(branchesCnt_promos)
        .append(" branches promos downloaded successfully.")
        .append(lineSeperator);
    if (aggType.equals(AggregatorType.daily)) {
      sb.append("(3) Stores-files: ").append(storesCnt)
          .append(" files were saved.").append(lineSeperator);
    }

    sb.append(lineSeperator).append(
        "Warning and Errors are logged to err.log at: " + this.sessionDir);

    return sb.toString();
  }

}
