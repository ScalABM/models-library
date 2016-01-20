package actors

import com.typesafe.config.Config


class RandomTraderConfig(config: Config) extends ZILiquiditySupplierConfig(config) {

  // arrival rate for order cancellations...
  val delta = config.getDouble("orderCancellationArrivalRate")

  // arrival rate for market orders...
  val mu = config.getDouble("marketOrderArrivalRate")

}
