package main;

import main.PricesAggregatorApp.ChainName;

public class DoralonAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "doralon";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.dor;
  }

}
