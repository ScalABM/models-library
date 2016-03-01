package actors


trait RandomLiquiditySupplierConfig extends RandomMarketParticipantConfig {

  val alpha = config.getDouble("limitOrderArrivalRate")

}

