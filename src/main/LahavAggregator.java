package main;

import main.PricesAggregatorApp.ChainName;

public class LahavAggregator extends NibitAggregator {

  @Override
  protected String getChainCode() {
    return "7290058179503";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.lahav;
  }

}
