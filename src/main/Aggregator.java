package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.PricesAggregatorApp.AggregatorType;
import main.PricesAggregatorApp.ChainName;

public abstract class Aggregator {

  protected Logger logger = Logger.getLogger(getClass().getName());
  protected String sessionDir;
  protected AggregatorType aggType;
  private Date startAgg;
  protected AggregateFileValidator fileValidator;

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
      e.printStackTrace();
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
      logger.severe("Exception in aggregatePricesAndPromos(): "
          + e.getMessage());
      e.printStackTrace();
    }

    // writing README file for this aggregation session
    try {
      PrintWriter out = new PrintWriter(sessionDir + "/" + "README.txt");
      out.println("Aggregator: " + getClass().getName());
      out.println("Start time: " + startAgg);
      out.println("End time: " + new Date());
      out.println("aggType: " + aggType);
      out.println();
      out.println("Description: ");
      out.println(getReadmeDescription());
      out.close();
    } catch (FileNotFoundException e) {
      System.err.println("Error while writing README file. " + e.getMessage());
      e.printStackTrace();
    }
  }

  protected abstract void aggregatePricesAndPromos();

  protected abstract String getReadmeDescription();

  protected abstract ChainName getChainName();

}
