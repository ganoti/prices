package main;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

public class MegaAggregator extends Aggregator {

  private static final String URL_MEGA = "http://publishprice.mega.co.il/";
  private String todayUrl;

  private int branchesCnt_prices = 0;
  private Set<String> branchesFailed_prices = new HashSet<String>();

  private int branchesCnt_promos = 0;
  private Set<String> branchesFailed_promos = new HashSet<String>();

  private int storesCnt = 0;

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

    try {
      SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyyMMdd");
      todayUrl = URL_MEGA + "/" + dayDateFormat.format(new Date());
      HtmlPage page = webClient.getPage(todayUrl);

      HtmlDivision tableDiv = (HtmlDivision) page.getElementById("files");
      HtmlTable filesTable =
          (HtmlTable) tableDiv.getElementsByTagName("table").get(0);

      List<HtmlTableRow> rows = filesTable.getRows();
      logger.info("Found " + rows.size() + " rows in file-table. ");
      for (HtmlTableRow row : rows) {
        HtmlTableCell tdc = row.getCells().size() > 0 ? row.getCell(0) : null;
        String fileName = null;
        if (tdc != null && tdc.getAttribute("class").equals("name")) {
          HtmlAnchor a = (HtmlAnchor) tdc.getElementsByTagName("a").get(0);
          fileName = a.getAttribute("href");

          if (fileValidator.shouldAggregateFile(fileName)) {
            if (aggType.equals(AggregatorType.daily)) {
              if (fileName.startsWith("PriceFull")) {
                a.click();
                branchesCnt_prices++;
              } else if (fileName.startsWith("PromoFull")) {
                a.click();
                branchesCnt_promos++;
              } else if (fileName.startsWith("Store")) {
                a.click();
                storesCnt++;
              }
            } else if (aggType.equals(AggregatorType.hourly)) {
              if (fileName.startsWith("Price")
                  && !fileName.startsWith("PriceFull")) {
                a.click();
                branchesCnt_prices++;
              } else if (fileName.startsWith("Promo")
                  && !fileName.startsWith("PromoFull")) {
                a.click();
                branchesCnt_promos++;
              }
            } else
              throw new AggregatorException("Not supported aggType: " + aggType);
          }
        }
      }

    } catch (Exception e) {
    	logSevere(e, "aggregatePricesAndPromos");
    } finally {
      webClient.close();
    }
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.mega;
  }

  @Override
  protected String getReadmeDescription() {
    String lineSeperator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Aggregated prices and promos from page : ")
            .append(todayUrl)
            .append(lineSeperator)
            .append(
                "Iterates the rows of the fileList table under \"files\" div. Notice that all the table is of the current date.")
            .append(lineSeperator)
            .append(
                "Branches are parsed from the file name, which is taken from the link href attrebute in each row.")
            .append(lineSeperator)
            .append(
                "Xml files are extracted from the .gz files, and are saved in their original name.")
            .append(lineSeperator)
            .append(
                " The gz files are: all gz files that start with either \"PriceFull\" or \"PromoFull\" in Daily mode, or with either \"Price\" or \"Promo\" (but do not continue with \"Full\"), in the hourly mode,")
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
