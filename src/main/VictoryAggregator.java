package main;

import main.PricesAggregatorApp.ChainName;

public class VictoryAggregator extends NibitAggregator {

  @Override
  protected String getChainCode() {
    return "7290696200003";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.victory;
  }

}
