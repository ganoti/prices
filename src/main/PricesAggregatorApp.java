package main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.*;

public class PricesAggregatorApp {

  public enum AggregatorType {
    daily, hourly
  }

  public enum ChainName {
    coop, rami, dosh, tiv, shuf, hazi, keshet, yohan, osher, dor, victory, lahav, hashuk, mega, bitan, eden
  }

  public static final String SUB_DIR_PRICES = "prices";
  public static final String SUB_DIR_PROMOS = "promos";
  public static final String PREFIX_STORES = "Stores";
  public static final String PREFIX_PRICE = "Price";
  public static final String PREFIX_PROMO = "Promo";

  private static AggregatorType aggType;
  private static ChainName chainName;

  static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  static Options generateCLIOptions() {
	  Options op = new Options();
	  op.addOption("debug","Output all information to STDOUT");
	  Option type = new Option("type","Download type. Either hourly or daily");
	  type.setArgs(1);
	  type.setRequired(true);
	  op.addOption(type);
	  
	  Option chain = new Option("chain","Chain name");
	  StringBuilder sb = new StringBuilder();
	  for (ChainName c : ChainName.values())
		  sb.append(String.format("%s,",c.toString()));
	  chain.setDescription(String.format("Chain name.\nPossibilities: %s", sb.toString()));
	  chain.setRequired(true);
	  chain.setArgs(1);
	  op.addOption(chain);
	  
	  return op;
  }
  
  public static Aggregator createAggregator(ChainName n) {
	  Aggregator result=  null;  
	  Hashtable<ChainName, Class<? extends Aggregator>> table = new Hashtable<PricesAggregatorApp.ChainName, Class<? extends Aggregator>>();;
	    table.put(ChainName.coop , CoopAggregator.class);
	    table.put(ChainName.bitan, BitanAggregator.class);
	    table.put(ChainName.dor, DoralonAggregator.class);
	    table.put(ChainName.dosh, DoshAggregator.class);
	    table.put(ChainName.eden, EdenAggregator.class);
	    table.put(ChainName.hashuk, HashukAggregator.class);
	    table.put(ChainName.hazi, HaziHinamAggregator.class);
	    table.put(ChainName.keshet, KeshetAggregator.class);
	    table.put(ChainName.lahav, LahavAggregator.class);
	    table.put(ChainName.mega, MegaAggregator.class);
	    table.put(ChainName.osher, OsheradAggregator.class);
	    table.put(ChainName.rami, RamiAggregator.class);
	    table.put(ChainName.shuf, ShufersalAggregator.class);
	    table.put(ChainName.tiv, TivTaamAggregator.class);
	    table.put(ChainName.victory, VictoryAggregator.class);
	    table.put(ChainName.yohan, YohananofAggregator.class);
	    
	    Class <? extends Aggregator> ag = table.get(n);
	  try {
		result = ag.newInstance();
	  } catch (Exception e) {
		//Could never happen
		e.printStackTrace();
	  } 
	  return result;
  }
  
  static void parseArgs(String [] args) {
	  try {
	    	CommandLineParser parser = new DefaultParser();
	        CommandLine line = parser.parse(generateCLIOptions(), args);
			if (line.hasOption("debug")) ; //Mark the debug flag
			
			String chain = line.getOptionValue("chain");
	        String agg   = line.getOptionValue("type");
			
			try {
	            aggType = Enum.valueOf(AggregatorType.class, agg.toLowerCase());
	          } catch (Exception e) {
	            throw new ParseException(
	                "Illegal aggtype arg. Two possible values: daily or hourly.");
	          }
			try {
	            chainName = Enum.valueOf(ChainName.class, chain.toLowerCase());
	          } catch (Exception e) {
	           
	            throw new ParseException("Illegal chain arg.");
	          }
			
	    } catch (ParseException e) {
	        System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
	        HelpFormatter h = new HelpFormatter();
			h.printHelp("PriceAggregator", generateCLIOptions());
	        System.exit(-1);;
		}
  }
  
  /**
   * 
   * @param args type=daily/hourly chain=coop/rami/...
   */
  public static void main(String args[]) {
    logger.setLevel(Level.WARNING);

    
    parseArgs(args);

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

    Aggregator ag = createAggregator(chainName);
    logger.info("Excecuting " + ag.getChainName() + " Aggregator. ");
    ag.aggregate(sessionPath);
    logger.info("Finished executing " + aggType + " aggregator for chain "
        + chainName);

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
