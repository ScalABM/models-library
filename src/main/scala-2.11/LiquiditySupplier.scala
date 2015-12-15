import akka.agent.Agent

import markets.orders.limit.LimitOrderLike
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global


/** A Mixin trait providing behavior necessary to generate and submit `LimitOrderLike` orders. */
trait LiquiditySupplier {
  this: TradingActor =>

  def generateLimitOrder(tradable: Tradable, ticker: Agent[Tick]): LimitOrderLike

  def submitLimitOrder(): Unit

  def scheduleLimitOrder(initialDelay: FiniteDuration): Unit = {
    context.system.scheduler.scheduleOnce(initialDelay)(submitLimitOrder())
  }

  def scheduleLimitOrder(initialDelay: FiniteDuration, interval: FiniteDuration): Unit = {
    context.system.scheduler.schedule(initialDelay, interval)(submitLimitOrder())
  }

}
