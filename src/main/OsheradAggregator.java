package main;

import main.PricesAggregatorApp.ChainName;

public class OsheradAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "osherad";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.osher;
  }

}
