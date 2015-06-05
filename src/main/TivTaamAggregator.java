package main;

import main.PricesAggregatorApp.ChainName;

public class TivTaamAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "TivTaam";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.tiv;
  }

}
