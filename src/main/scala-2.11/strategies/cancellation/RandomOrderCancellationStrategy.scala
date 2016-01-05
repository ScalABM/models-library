package strategies.cancellation

import markets.orders.Order
import markets.participants.strategies.OrderCancellationStrategy

import scala.collection.mutable
import scala.util.Random


class RandomOrderCancellationStrategy(prng: Random) extends OrderCancellationStrategy {

  override def cancelOneOf[T <: mutable.Iterable[Order]](outstandingOrders: T): Option[Order] = {
    if (outstandingOrders.isEmpty) None else Some(outstandingOrders.toIndexedSeq(prng.nextInt(outstandingOrders.size)))
  }

}
