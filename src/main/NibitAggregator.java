package main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.PricesAggregatorApp.AggregatorType;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
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

      page = selectChain(page, getChainCode());

      setCurrentDate(page);

      page = hitSearchButton(page);
      int js = webClient.waitForBackgroundJavaScript(0);
      while (js > 1) {
        js = webClient.waitForBackgroundJavaScript(1000);
        logger.info("Javascript processes: " + js + ". still waiting...");
      }

      HtmlDivision tableDiv =
          (HtmlDivision) page.getElementById("download_content");
      HtmlTable filesTable =
          (HtmlTable) tableDiv.getElementsByTagName("table").get(0);

      List<HtmlTableRow> rows = filesTable.getRows();
      logger.info("Found " + rows.size() + " rows in file-table. ");
      for (HtmlTableRow row : rows) {
        String fileName = parseNibitFileName(row.getCell(0).getTextContent());
        if (fileName != null) {
          if (fileValidator.shouldAggregateFile(fileName)) {
            if (aggType.equals(AggregatorType.daily)) {
              if (fileName.startsWith("PriceFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_prices++;
              } else if (fileName.startsWith("PromoFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_promos++;
              } else if (fileName.startsWith("Store")) {
                downloadFromRow(row, webClient, page);
                storesCnt++;
              }
            } else if (aggType.equals(AggregatorType.hourly)) {
              if (fileName.startsWith("Price")
                  && !fileName.startsWith("PriceFull")) {
                downloadFromRow(row, webClient, page);
                branchesCnt_prices++;
              } else if (fileName.startsWith("Promo")
                  && !fileName.startsWith("PromoFull")) {
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

  private void downloadFromRow(HtmlTableRow row, AggregatorWebClient webClient,
      HtmlPage page) throws IOException {
    HtmlAnchor a = (HtmlAnchor) row.getElementsByTagName("a").get(0);
    a.click();
    // Page page2 = a.click();
    // webClient.handleContentChanged(page2.getWebResponse());
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
    // TODO Auto-generated method stub
    return null;
  }

}