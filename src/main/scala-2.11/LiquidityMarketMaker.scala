/** A Mixin trait providing behavior necessary to generate and submit both `LimitOrderLike` and
  * `MarketOrderLike` orders.
  */
trait LiquidityMarketMaker {
  this: TradingActor with LiquidityDemander with LiquiditySupplier =>

}
