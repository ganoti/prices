package main;

import main.PricesAggregatorApp.ChainName;

public class KeshetAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "Keshet";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.keshet;
  }

}
