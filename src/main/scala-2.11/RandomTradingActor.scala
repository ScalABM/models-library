
import markets.Cancel
import markets.orders.Order

import scala.collection.immutable
import scala.util.Random


trait RandomTradingActor extends TradingActor {

  def config: RandomTraderConfig

  def prng: Random

  def askPrice(prng: Random, lower: Long, upper: Long): Long = {
    RandomTradingActor.uniform(prng, lower, upper)
  }

  def bidPrice(prng: Random, lower: Long, upper: Long): Long = {
    RandomTradingActor.uniform(prng, lower, upper)
  }

  def askQuantity(prng: Random, lower: Long, upper: Long): Long = {
    RandomTradingActor.uniform(prng, lower, upper)
  }

  def bidQuantity(prng: Random, lower: Long, upper: Long): Long = {
    RandomTradingActor.uniform(prng, lower, upper)
  }

  def generateOrderCancellation(orders: immutable.Iterable[Order]): Option[Cancel] = {
    val order = prng.shuffle(orders).headOption
    order match {
      case Some(outstandingOrder) =>
        Some(Cancel(outstandingOrder, timestamp(), uuid()))
      case None =>
        None
    }
  }

  def submitOrderCancellation(): Unit = {
    val orderCancellation = generateOrderCancellation(outstandingOrders)
    orderCancellation match {
      case Some(cancellation) =>
        val (market, _) = markets(cancellation.order.tradable)
        market ! cancellation
      case None => // no outstanding orders to cancel!
    }
  }
}


object RandomTradingActor {

  def uniform(prng: Random, lower: Long, upper: Long): Long = {
    (lower + (upper - lower) * prng.nextDouble()).toLong
  }

}