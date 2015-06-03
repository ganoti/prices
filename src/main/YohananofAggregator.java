package main;

import main.PricesAggregatorApp.ChainName;

public class YohananofAggregator extends CerberusAggregator {

  @Override
  protected String getUsername() {
    return "yohananof";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.yohan;
  }

}
