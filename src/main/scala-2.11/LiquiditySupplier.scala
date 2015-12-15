import akka.agent.Agent

import markets.orders.limit.LimitOrderLike
import markets.tickers.Tick
import markets.tradables.Tradable


/** A Mixin trait providing behavior necessary to generate and submit `LimitOrderLike` orders. */
trait LiquiditySupplier {
  this: TradingActor =>
  
  def generateLimitOrder(tradable: Tradable, ticker: Agent[Tick]): LimitOrderLike

}
