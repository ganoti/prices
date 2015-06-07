package main;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

public class EdenAggregator extends Aggregator {
  
  private static final String URL_EDEN = "http://operations.edenteva.co.il/Prices/index";
  
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
      HtmlPage page = webClient.getPage(URL_EDEN);
      DomNodeList<DomElement> anchorList = page.getElementsByTagName("a");
      HtmlAnchor a;
      String fileName;
      for (DomElement domElement : anchorList) {
        a = (HtmlAnchor) domElement;
        if (a.getTextContent().endsWith(".zip")) {
          fileName = a.getTextContent();
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
    return ChainName.eden;
  }

  @Override
  protected String getReadmeDescription() {
    String lineSeperator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Aggregated prices and promos from page : ")
            .append(URL_EDEN)
            .append(lineSeperator)
            .append(
                "Iterates over all html-anchors (elements with tag name \"a\" found in the page), of which their text-content ends with \"zip\".")
            .append(lineSeperator)
            .append(
                "This content is the file name, and branches as well as time-stamps are parsed from these file names.")
            .append(lineSeperator)
            .append(
                "Xml files are extracted from the .zip files, and are saved in their original name.")
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
