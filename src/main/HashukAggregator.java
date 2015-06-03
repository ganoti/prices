package main;

import main.PricesAggregatorApp.ChainName;

public class HashukAggregator extends NibitAggregator {

  @Override
  protected String getChainCode() {
    return "7290661400001";
  }

  @Override
  protected ChainName getChainName() {
    return ChainName.hashuk;
  }

}
