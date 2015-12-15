
import akka.agent.Agent

import markets.orders.market.MarketOrderLike
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global


/** A Mixin trait providing behavior necessary to generate and submit `MarketOrderLike` orders. */
trait LiquidityDemander {
  this: TradingActor =>

  def generateMarketOrder(tradable: Tradable, ticker: Agent[Tick]): MarketOrderLike

  def submitMarketOrder(): Unit

  def scheduleMarketOrder(initialDelay: FiniteDuration): Unit = {
    context.system.scheduler.scheduleOnce(initialDelay)(submitMarketOrder())
  }

  def scheduleMarketOrder(initialDelay: FiniteDuration, interval: FiniteDuration): Unit = {
    context.system.scheduler.schedule(initialDelay, interval)(submitMarketOrder())
  }

}
