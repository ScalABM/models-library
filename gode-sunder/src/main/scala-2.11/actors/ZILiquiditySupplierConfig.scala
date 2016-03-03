package actors

import com.typesafe.config.Config


case class ZILiquiditySupplierConfig(config: Config) {

  val alpha = config.getDouble("limitOrderArrivalRate")

  val timeUnit = config.getString("timeUnit")

}
