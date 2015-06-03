package main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PricesAggregatorApp {

  public enum AggregatorType {
    daily, hourly
  }

  public enum ChainName {
    coop, rami, dosh, tiv, shuf, hazi, keshet, yohan, osher, dor, victory, lahav, hashuk, mega, bitan// TODO
                                                                                                     // eden
  }

  public static final String SUB_DIR_PRICES = "prices";
  public static final String SUB_DIR_PROMOS = "promos";
  public static final String PREFIX_STORES = "Stores";
  public static final String PREFIX_PRICE = "Price";
  public static final String PREFIX_PROMO = "Promo";

  private static AggregatorType aggType;
  private static ChainName chainName;

  static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  /**
   * 
   * @param args type=daily/hourly chain=coop/rami/...
   */
  public static void main(String args[]) {

    logger.setLevel(Level.WARNING);

    if (!parseArgs(args))
      return;

    /** determine session path **/

    Date date = new Date();
    SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat hourDateFormat = new SimpleDateFormat("HH");

    String sessionPath = "target/" + aggType + "/";
    sessionPath += dayDateFormat.format(date) + "/";
    File path = new File(sessionPath);
    if (!path.exists())
      path.mkdir();

    if (aggType.equals(AggregatorType.hourly)) {
      sessionPath += hourDateFormat.format(date) + "/";
      path = new File(sessionPath);
      if (!path.exists())
        path.mkdir();
    }

    sessionPath += chainName;

    path = new File(sessionPath);
    if (!path.exists())
      path.mkdir();

    /** Executing the chain's aggregator **/

    switch (chainName) {
    case coop:
      CoopAggregator coop = new CoopAggregator();
      logger.info("Excecuting " + coop.getChainName() + " Aggregator. ");
      coop.aggregate(sessionPath);
      break;
    case rami:
      RamiAggregator rami = new RamiAggregator();
      logger.info("Excecuting " + rami.getChainName() + " Aggregator. ");
      rami.aggregate(sessionPath);
      break;
    case dosh:
      DoshAggregator dosh = new DoshAggregator();
      logger.info("Excecuting " + dosh.getChainName() + " Aggregator. ");
      dosh.aggregate(sessionPath);
      break;
    case shuf:
      ShufersalAggregator shuf = new ShufersalAggregator();
      logger.info("Excecuting " + shuf.getChainName() + " Aggregator. ");
      shuf.aggregate(sessionPath);
      break;
    case tiv:
      TivTaamAggregator tiv = new TivTaamAggregator();
      logger.info("Excecuting " + tiv.getChainName() + " Aggregator. ");
      tiv.aggregate(sessionPath);
      break;
    case hazi:
      HaziHinamAggregator hazi = new HaziHinamAggregator();
      logger.info("Excecuting " + hazi.getChainName() + " Aggregator. ");
      hazi.aggregate(sessionPath);
      break;
    case keshet:
      KeshetAggregator keshet = new KeshetAggregator();
      logger.info("Excecuting " + keshet.getChainName() + " Aggregator. ");
      keshet.aggregate(sessionPath);
      break;
    case yohan:
      YohananofAggregator yohan = new YohananofAggregator();
      logger.info("Excecuting " + yohan.getChainName() + " Aggregator. ");
      yohan.aggregate(sessionPath);
      break;
    case osher:
      OsheradAggregator osher = new OsheradAggregator();
      logger.info("Excecuting " + osher.getChainName() + " Aggregator. ");
      osher.aggregate(sessionPath);
      break;
    case dor:
      DoralonAggregator dor = new DoralonAggregator();
      logger.info("Excecuting " + dor.getChainName() + " Aggregator. ");
      dor.aggregate(sessionPath);
      break;
    case victory:
      VictoryAggregator victory = new VictoryAggregator();
      logger.info("Excecuting " + victory.getChainName() + " Aggregator. ");
      victory.aggregate(sessionPath);
      break;
    case lahav:
      LahavAggregator lahav = new LahavAggregator();
      logger.info("Excecuting " + lahav.getChainName() + " Aggregator. ");
      lahav.aggregate(sessionPath);
      break;
    case hashuk:
      HashukAggregator hashuk = new HashukAggregator();
      logger.info("Excecuting " + hashuk.getChainName() + " Aggregator. ");
      hashuk.aggregate(sessionPath);
      break;
    case mega:
      MegaAggregator mega = new MegaAggregator();
      logger.info("Excecuting " + mega.getChainName() + " Aggregator. ");
      mega.aggregate(sessionPath);
      break;
    case bitan:
      BitanAggregator bitan = new BitanAggregator();
      logger.info("Excecuting " + bitan.getChainName() + " Aggregator. ");
      bitan.aggregate(sessionPath);
      break;
    default:
      logger.severe("No aggregation instructions for chain " + chainName
          + ". Provide instruction in main application, and try again.");
      break;
    }

    logger.info("Finished executing " + aggType + " aggregator for chain "
        + chainName);

  }

  private static boolean parseArgs(String[] args) {

    String usageStr = "USAGE: aggtype=<aggregator-type> chain=<chain-name>";

    if (args.length != 2) {
      logger.severe(usageStr);
      return false;
    }

    try {
      String[] tmp;
      String argKey;
      String argVal;
      for (String arg : args) {
        tmp = arg.split("=");
        argKey = tmp[0].trim();
        argVal = tmp[1].trim();
        switch (argKey) {
        case "aggtype":
          try {
            aggType = Enum.valueOf(AggregatorType.class, argVal.toLowerCase());
          } catch (Exception e) {
            throw new Exception(
                "Illegal aggtype arg. Two possible values: daily or hourly.");
          }
          break;
        case "chain":
          try {
            chainName = Enum.valueOf(ChainName.class, argVal.toLowerCase());
          } catch (Exception e) {
            ChainName[] chains = ChainName.values();
            StringBuilder sb = new StringBuilder();
            for (ChainName chainName : chains) {
              sb.append(chainName + "; ");
            }
            throw new Exception("Illegal chain arg. Possible values: "
                + sb.toString());
          }
          break;
        default:
          break;
        }
      }
    } catch (Exception e) {
      logger.severe(e.getMessage());
      logger.severe(usageStr);
      return false;
    }

    return true;
  }

  public synchronized static String getDaySubSessionPath(String aggSessionPath) {
    // the day sub-session path is after the third "/"
    int index = -1;
    for (int i = 0; i < 3; i++) {
      index = aggSessionPath.indexOf("/", index + 1);
    }
    return aggSessionPath.substring(0, index + 1);
  }

}
