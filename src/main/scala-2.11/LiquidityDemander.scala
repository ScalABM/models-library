
import markets.orders.market.MarketOrderLike
import markets.tradables.Tradable


/** A Mixin trait providing behavior necessary to generate and submit `MarketOrderLike` orders. */
trait LiquidityDemander {
  this: TradingActor =>

  def generateMarketOrder(tradable: Tradable): MarketOrderLike

}
