package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

public abstract class Aggregator {

  private static final String VERSION = "1.1";
  protected Logger logger = Logger.getLogger(getClass().getName());
  protected String sessionDir;
  protected AggregatorType aggType;
  private Date startAgg;
  protected AggregateFileValidator fileValidator;

  void logSevere(Exception e, String funcName) {
    logger.severe(String.format("%s() caught exception: %s , %s.\n %s\n",
        funcName, e.getClass().toString(), e.getMessage(),
        stacktraceToString(e)));
  }

  public static String stacktraceToString(Exception e) {
    StringWriter w = new StringWriter();
    PrintWriter pw = new PrintWriter(w);
    e.printStackTrace(pw);
    return w.toString();
  }

  public void aggregate(String sessionDir) {
    this.startAgg = new Date();
    this.sessionDir = sessionDir;

    try {
      FileHandler fh = new FileHandler(sessionDir + "/" + "err.log", true);
      fh.setEncoding("UTF-8");
      fh.setFormatter(new LogTxtFormatter());
      logger.addHandler(fh);
      logger.setLevel(Level.WARNING);
    } catch (SecurityException | IOException e) {
      logSevere(e, "aggregate");
    }

    if (sessionDir.contains(AggregatorType.daily.toString())) {
      aggType = AggregatorType.daily;
    } else if (sessionDir.contains(AggregatorType.hourly.toString())) {
      aggType = AggregatorType.hourly;
    } else
      throw new AggregatorException(
          "Could not determine aggregator type. Expected "
              + AggregatorType.daily.toString() + " or "
              + AggregatorType.hourly.toString());

    try {
      fileValidator =
          new AggregateFileValidator(aggType, getChainName(), sessionDir);
      aggregatePricesAndPromos();
    } catch (Exception e) {
      logSevere(e, "aggregatePricesAndPromos");
    }

    // writing README file for this aggregation session
    try {
      PrintWriter out = new PrintWriter(sessionDir + "/" + "README.txt");
      out.println("Aggregator: " + getClass().getName());
      out.println("Software version: " + VERSION);
      out.println("Start time: " + startAgg);
      out.println("End time: " + new Date());
      out.println("aggType: " + aggType);
      out.println();
      out.println("Description: ");
      out.println(getReadmeDescription());
      out.println(getSoftwareVersionDescription());
      out.close();
    } catch (FileNotFoundException e) {
      logSevere(e, "aggregate");
    }
  }

  private String getSoftwareVersionDescription() {
    String lineSeparator = System.getProperty("line.separator");
    StringBuilder sb =
        new StringBuilder()
            .append("Software version: " + VERSION)
            .append(lineSeparator)
            .append(
                "Added Eden aggregator and fixed Shufersal (and changed 3 Nibit aggregators, but they are still not correct, see their README for more details.).")
            .append(lineSeparator);
    return sb.toString();
  }

  protected abstract void aggregatePricesAndPromos();

  protected abstract String getReadmeDescription();

  protected abstract ChainName getChainName();

}
