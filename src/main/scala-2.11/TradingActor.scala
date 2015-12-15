import markets.orders.Order
import markets.{Cancel, BaseActor}
import markets.participants.MarketParticipantLike

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global


trait TradingActor extends BaseActor with MarketParticipantLike {

  def generateOrderCancellation(orders: immutable.Iterable[Order]): Option[Cancel]

  def submitOrderCancellation(): Unit

  def scheduleOrderCancellation(initialDelay: FiniteDuration): Unit = {
    context.system.scheduler.scheduleOnce(initialDelay)(submitOrderCancellation())
  }

  def scheduleOrderCancellation(initialDelay: FiniteDuration, interval: FiniteDuration): Unit = {
    context.system.scheduler.schedule(initialDelay, interval)(submitOrderCancellation())
  }
}
