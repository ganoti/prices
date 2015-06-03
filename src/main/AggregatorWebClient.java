package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import main.PricesAggregatorApp.AggregatorType;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlUtil;

public class AggregatorWebClient extends WebClient {

  private static final long serialVersionUID = 1L;

  private String outputDir;

  Logger logger;

  public AggregatorWebClient(BrowserVersion bv, String outputDir,
      AggregatorType aggType, Logger logger) {
    super(bv);

    this.outputDir = outputDir;
    this.logger = logger;
    getOptions().setThrowExceptionOnScriptError(false);

    ConfirmHandler okHandler = new ConfirmHandler() {
      public boolean handleConfirm(Page page, String message) {
        return true;
      }
    };

    setConfirmHandler(okHandler);
    getOptions().setJavaScriptEnabled(true);
    setAjaxController(new NicelyResynchronizingAjaxController());

    addWebWindowListener(new WebWindowListener() {

      @Override
      public void webWindowOpened(WebWindowEvent event) {
      }

      @Override
      public void webWindowContentChanged(WebWindowEvent event) {
        handleContentChanged(event.getNewPage().getWebResponse());
      }

      @Override
      public void webWindowClosed(WebWindowEvent event) {
      }
    });

    // silent log
    setCssErrorHandler(new SilentCssErrorHandler());
    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
        "org.apache.commons.logging.impl.NoOpLog");
    java.util.logging.Logger.getLogger("org.apache").setLevel(Level.OFF);
    java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
        Level.OFF);
  }

  public void handleContentChanged(WebResponse response) {
    String contentType = response.getContentType();
    if (contentType.startsWith("text/xml")) {
      handleXmlResponse(response);
    } else if (contentType.equals("application/octet-stream")
        || contentType.equals("application/x-gzip")
        || contentType.equals("application/zip")) {
      handleCompressedFileResponse(response);
    }
  }

  public void handleCompressedFileResponse(WebResponse response) {
    String fileName = getCompressedFileName(response);

    if (fileName.endsWith(".gz") || fileName.endsWith("zip")) {
      InputStream inputStream = null;
      OutputStream outputStream = null;
      InflaterInputStream inflaterInputStream = null;
      if (fileName.isEmpty())
        return;
      try {
        inputStream = response.getContentAsStream();
        if (fileName.endsWith(".gz"))
          inflaterInputStream = new GZIPInputStream(inputStream);
        else
          inflaterInputStream = new ZipInputStream(inputStream);
        File dir;
        if (!fileName.startsWith("Stores"))
          dir =
              new File(outputDir + "/"
                  + AggregateFileValidator.getBranchId(fileName));
        else
          dir = new File(outputDir);

        if (!dir.exists()) {
          dir.mkdir();
        }
        String fullPath = dir.getPath() + "/" + fileName;
        fullPath = fullPath.substring(0, fullPath.lastIndexOf("."));
        if (!fullPath.endsWith(".xml"))
          fullPath += ".xml";
        outputStream = new FileOutputStream(fullPath);
        int read = 0;
        byte[] bytes2 = new byte[1024];
        while ((read = inflaterInputStream.read(bytes2)) != -1) {
          outputStream.write(bytes2, 0, read);
        }
      } catch (IOException e) {
        logger.severe("Exception while saving gz file " + fileName + "\n"
            + e.getClass() + ", " + e.getMessage());
        e.printStackTrace();
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            logger.severe(e.getClass() + ", " + e.getMessage());
            e.printStackTrace();
          }
        }
        if (outputStream != null) {
          try {
            outputStream.close();
          } catch (IOException e) {
            logger.severe(e.getClass() + ", " + e.getMessage());
            e.printStackTrace();
          }
        }
      }
    }
  }

  // default: no GZIP
  protected String getCompressedFileName(WebResponse response) {
    return null;
  }

  public void handleXmlResponse(WebResponse response) {
    InputStream inputStream = null;
    OutputStream outputStream = null;
    try {

      inputStream = response.getContentAsStream();
      String fileName;
      try {
        // Coop xmls are taken from header.
        String fileNameVal =
            response.getResponseHeaders()
                .get(response.getResponseHeaders().size() - 1).getValue();
        fileName =
            fileNameVal.substring(fileNameVal.indexOf("filename=")
                + "filename=".length());
      } catch (Exception e) {
        // for Stores files of Cerberus server - take file name from URL
        String t = response.getWebRequest().getUrl().getFile();
        fileName = t.substring(t.lastIndexOf("/") + 1);
      }

      File branchDir;
      if (!fileName.startsWith(PricesAggregatorApp.PREFIX_STORES)) {
        String branchId = getBranchFromRequestParams(response);
        branchDir = new File(outputDir + "/" + branchId);
      } else
        branchDir = new File(outputDir);

      if (!branchDir.exists()) {
        branchDir.mkdir();
      }
      outputStream = new FileOutputStream(branchDir.getPath() + "/" + fileName);

      int read = 0;
      byte[] bytes = new byte[1024];

      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }

      // checking if this xml can be parsed without errors.
      // (this is a patch: better if this warning will be taken from
      // XmlPage<init>)
      try {
        // TODO add this check also for the gz download
        XmlUtil.buildDocument(response);
      } catch (SAXException | ParserConfigurationException e) {
        logger.warning("Failed parsing XML document " + fileName + ": "
            + e.getMessage());
      }
    } catch (IOException e) {
      logger.severe("Exception while saving xml. " + e.getClass() + ", "
          + e.getMessage());
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logger.severe(e.getClass() + ", " + e.getMessage());
          e.printStackTrace();
        }
      }
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          logger.severe(e.getClass() + ", " + e.getMessage());
          e.printStackTrace();
        }

      }
    }
  }

  private String getBranchFromRequestParams(WebResponse response) {
    String branchId = null;
    List<NameValuePair> requestParams =
        response.getWebRequest().getRequestParameters();
    for (NameValuePair param : requestParams) {
      if (param.getName().equals("branch")) {
        branchId = param.getValue();
        break;
      }
    }
    if (branchId == null)
      throw new AggregatorException(
          "Could not find the branch parameter in WebRequest. ");

    return branchId;
  }
}
