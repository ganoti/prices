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

  @Override
  protected void aggregatePricesAndPromos() {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected ChainName getChainName() {
    // TODO Auto-generated method stub
    return null;
  }

}
