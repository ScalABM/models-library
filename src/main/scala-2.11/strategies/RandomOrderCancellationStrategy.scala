package strategies

import markets.orders.Order
import markets.participants.strategies.OrderCancellationStrategy

import scala.collection.mutable
import scala.util.Random


class RandomOrderCancellationStrategy(prng: Random) extends OrderCancellationStrategy {

  def cancelOneOf[T <: mutable.IndexedSeq[Order]](outstandingOrders: T): Option[Order] = {
    if (outstandingOrders.isEmpty) None else Some(outstandingOrders(prng.nextInt(outstandingOrders.size)))
  }

}
