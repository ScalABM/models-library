package strategies.trading

import com.typesafe.config.Config


trait ZILimitOrderTradingStrategyConfig extends ZITradingStrategyConfig {

  def config: Config

  // lower bound on the support of the ask order price distribution
  val minAskPrice = config.getLong("minAskPrice")

  // lower bound on the support of the bid order price distribution
  val minBidPrice = config.getLong("minBidPrice")

  // upper bound on the support of the ask order price distribution
  val maxAskPrice = config.getLong("maxAskPrice")

  // upper bound on the support of the bid order price distribution
  val maxBidPrice = config.getLong("maxBidPrice")

}
