package main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.PricesAggregatorApp.AggregatorType;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * This aggregator is not working correctly yet. For some reason clicking the
 * download link in a row on the page does not trigger the
 * webWindowContentChanged method in AggregatorWebClient
 */
public abstract class NibitAggregator extends Aggregator {

  private static final String URL_NIBIT =
      "http://matrixcatalog.co.il/NBCompetitionRegulations.aspx";

  private int pricesCnt = 0;
  private Set<String> branchesFailed_prices = new HashSet<String>();

  private int promosCnt = 0;
  private Set<String> branchesFailed_promos = new HashSet<String>();

  private int storesCnt = 0;

  private String lastAggFile = "";

  @Override
  protected void aggregatePricesAndPromos() {
    String path = sessionDir;

    AggregatorWebClient webClient =
        new AggregatorWebClient(BrowserVersion.FIREFOX_31, path, aggType,
            logger) {
          private static final long serialVersionUID = 1L;

          @Override
          protected String getCompressedFileName(WebResponse response) {
            String fileName = null;
            List<NameValuePair> headerList = response.getResponseHeaders();
            for (NameValuePair pair : headerList) {
              if (pair.getValue().contains("filename=")) {
                String val = pair.getValue();
                fileName =
                    val.substring(val.indexOf("filename=")
                        + "filename=".length());
                return parseNibitFileName(fileName)
                    + fileName.substring(fileName.indexOf("."));
              }
            }
            return fileName;
          }
        };

    try {
      HtmlPage page = webClient.getPage(URL_NIBIT);

      // page = selectChain(page, getChainCode());

      // setCurrentDate(page);

      // page = hitSearchButton(page);
      // int js = webClient.waitForBackgroundJavaScript(0);
      // while (js > 1) {
      // js = webClient.waitForBackgroundJavaScript(1000);
      // logger.info("Javascript processes: " + js + ". still waiting...");
      // }

      HtmlAnchor a;
      String fileName;
      boolean finish = false;

      while (!finish) {
        HtmlDivision tableDiv =
            (HtmlDivision) page.getElementById("download_content");
        HtmlTable filesTable =
            (HtmlTable) tableDiv.getElementsByTagName("table").get(0);

        DomNodeList<HtmlElement> anchorList =
            filesTable.getElementsByTagName("a");

        for (DomElement domElement : anchorList) {
          if (finish)
            break;
          // TODO fix: Some of other's Nibit chains will be collected, and maybe
          // not all this Nibit chain files are collected. Need to see why.
          fileName = null;
          a = (HtmlAnchor) domElement;
          if (a.getParentNode().getNodeName().equals("td")
              && a.getParentNode().getParentNode().getNodeName().equals("tr")) {
            fileName =
                parseNibitFileName(((HtmlTableRow) a.getParentNode()
                    .getParentNode()).getCell(0).getTextContent());
          }

          if (fileName != null) {
            if (!fileValidator.isFromToday(fileName)) {
              finish = true; // stop iterating forward on table's pages
            } else if (!fileValidator.shouldAggregateFile(fileName)) {
              finish = true;
            } else if (fileName.contains(getChainCode())) {
              boolean downloaded = true;
              if (aggType.equals(AggregatorType.daily)) {
                if (fileName.startsWith("PriceFull")) {
                  a.click();
                  pricesCnt++;
                } else if (fileName.startsWith("PromoFull")) {
                  a.click();
                  promosCnt++;
                } else if (fileName.startsWith("Store")) {
                  a.click();
                  storesCnt++;
                } else {
                  downloaded = false;
                }
              } else if (aggType.equals(AggregatorType.hourly)) {
                if (fileName.startsWith("Price")
                    && !fileName.startsWith("PriceFull")) {
                  a.click();
                  pricesCnt++;
                } else if (fileName.startsWith("Promo")
                    && !fileName.startsWith("PromoFull")) {
                  a.click();
                  promosCnt++;
                } else {
                  downloaded = false;
                }
              } else {
                throw new AggregatorException("Not supported aggType: "
                    + aggType);
              }
              if (downloaded) {
                webClient.getWebWindows().get(0).getHistory().back();
                lastAggFile = fileName;
              }
            }
          }
        }

        if (!finish) {
          // move to next table page
          if (page.getElementById("MainContent_btnNext2") != null
              && !((HtmlSubmitInput) page
                  .getElementById("MainContent_btnNext2")).isDisabled()) {
            page =
                ((HtmlSubmitInput) page.getElementById("MainContent_btnNext2"))
                    .click();
          } else {
            finish = true;
          }
        }
      }
    } catch (Exception e) {
      logSevere(e, "aggregatePricesAndPromos");
    }

  }

  private String parseNibitFileName(String textContent) {
    try {
      String[] t = textContent.split("-");
      if (t[0].startsWith("Store")) {
        logger
            .severe("Nibit provided Stores files - check how to parse them... File Name: "
                + textContent);
        return null;
      } else {
        StringBuilder sb =
            new StringBuilder().append(t[0]).append(t[1]).append("-")
                .append(t[2]).append("-").append(t[3]);
        return sb.toString();
      }
    } catch (Exception e) {
      return null;
    }
  }

  private HtmlPage hitSearchButton(HtmlPage page) {
    try {
      return ((HtmlSubmitInput) page.getElementsByIdAndOrName(
          "ctl00$MainContent$btnSearch").get(0)).click();
    } catch (Exception e) {
      throw new RuntimeException("Error while clicking the search button.", e);
    }
  }

  private void setCurrentDate(HtmlPage page) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      ((HtmlTextInput) page.getElementsByIdAndOrName(
          "ctl00$MainContent$txtDate").get(0)).setText(dateFormat
          .format(new Date()));
    } catch (Exception e) {
      throw new RuntimeException("Error while setting the current date.", e);
    }
  }

  private HtmlPage selectChain(HtmlPage page, String chainCode) {
    HtmlSelect select1;
    try {
      select1 =
          (HtmlSelect) page.getElementsByIdAndOrName("ctl00$MainContent$chain")
              .get(0);
    } catch (Exception e) {
      throw new RuntimeException(
          "Did not find ctl00$MainContent$chain element.", e);
    }

    HtmlOption chainOption = select1.getOptionByValue(chainCode);

    return (HtmlPage) chainOption.setSelected(true);
  }

  protected abstract String getChainCode();

  @Override
  protected String getReadmeDescription() {
    String lineSeperator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Aggregated prices and promos from page : ")
            .append(URL_NIBIT)
            .append(lineSeperator)
            .append("For chain code: ")
            .append(getChainCode())
            .append(", name: ")
            .append(getChainName())
            .append(lineSeperator)
            .append(
                "Iterates over the links in the main table, and aggregates:")
            .append(lineSeperator)
            .append("In Daily mode: ")
            .append(
                "PromosFull, PricesFull and Stores files, of the current day and which have not yet been ")
            .append("aggregated earlier today,")
            .append(lineSeperator)
            .append(
                "And in Hourly mode: same as daily mode, but files starting with Promos and Prices.")
            .append(lineSeperator)
            .append(
                "Assumes the rows in table are sorted in decreasing order of time, and stop iterating over the table-pages when reach a row from yesterday")
            .append(lineSeperator)
            .append("Last aggregated file: ")
            .append(lastAggFile)
            .append(
                "Time stamp and branches are parsed from the value in the \"Name\" column.")
            .append(lineSeperator)
            .append(
                "We don't filter the table because the links forced us to press the \"back\" button in webClient, and this resets the filter. ")
            .append(lineSeperator)
            .append(lineSeperator)
            .append(
                "Notice: after the first table-page does not work correctly. Some of other's Nibit chains will be collected, and maybe not all this Nibit chain files are collected. ")
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