package actors


trait RandomLiquidityDemanderConfig extends RandomMarketParticipantConfig {

  val mu = config.getDouble("marketOrderArrivalRate")

}

