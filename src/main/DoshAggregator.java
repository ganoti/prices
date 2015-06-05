package main;

import main.PricesAggregatorApp.ChainName;

public class DoshAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "SuperDosh";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.dosh;
  }

}
