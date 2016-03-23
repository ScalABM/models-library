package strategies.trading

import com.typesafe.config.Config


trait ZITradingStrategyConfig {

  def config: Config

  // lower bound on the support of the ask order quantity distribution
  val minAskQuantity = config.getLong("minAskQuantity")

  // lower bound on the support of the bid order quantity distribution
  val minBidQuantity = config.getLong("minBidQuantity")

  // upper bound on the support of the ask order quantity distribution
  val maxAskQuantity = config.getLong("maxAskQuantity")

  // upper bound on the support of the bid order quantity distribution
  val maxBidQuantity = config.getLong("maxBidQuantity")


}
