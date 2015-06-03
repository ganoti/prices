package main;

import main.PricesAggregatorApp.ChainName;

public class HaziHinamAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "HaziHinam";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.hazi;
  }

}
