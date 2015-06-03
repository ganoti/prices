package main;

import main.PricesAggregatorApp.ChainName;

public class RamiAggregator extends CerberusAggregator {

  @Override
  protected ChainName getChainName() {
    return ChainName.rami;
  }

  @Override
  protected String getUsername() {
    return "RamiLevi";
  }

}
