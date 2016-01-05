package actors

import com.typesafe.config.Config


class RandomTraderConfig(config: Config) {

  // arrival rate for limit orders...
  val alpha = config.getDouble("limitOrderArrivalRate")

  // arrival rate for order cancellations...
  val delta = config.getDouble("orderCancellationArrivalRate")

  // arrival rate for market orders...
  val mu = config.getDouble("marketOrderArrivalRate")

  val askOrderProb = config.getDouble("askOrderProbability")

  // lower bound on the support of the ask order price distribution
  val minAskPrice = config.getLong("minAskPrice")

  // lower bound on the support of the bid order price distribution
  val minBidPrice = config.getLong("minBidPrice")

  // upper bound on the support of the ask order price distribution
  val maxAskPrice = config.getLong("maxAskPrice")

  // upper bound on the support of the bid order price distribution
  val maxBidPrice = config.getLong("maxBidPrice")

  // lower bound on the support of the ask order quantity distribution
  val minAskQuantity = config.getLong("minAskQuantity")

  // lower bound on the support of the bid order quantity distribution
  val minBidQuantity = config.getLong("minBidQuantity")

  // upper bound on the support of the ask order quantity distribution
  val maxAskQuantity = config.getLong("maxAskQuantity")

  // upper bound on the support of the bid order quantity distribution
  val maxBidQuantity = config.getLong("maxBidQuantity")

}
