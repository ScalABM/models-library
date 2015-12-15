import akka.agent.Agent

import markets.orders.limit.{LimitBidOrder, LimitAskOrder, LimitOrderLike}
import markets.tickers.Tick
import markets.tradables.Tradable


/** Zero Intelligence (ZI) liquidity supplier.
  *
  * @note A ZI liquidity supplier randomly generates `LimitOrderLike` orders.
  */
trait ZILiquiditySupplier extends LiquiditySupplier {
  this: RandomTradingActor =>

  def generateLimitOrder(tradable: Tradable, ticker: Agent[Tick]): LimitOrderLike = {
    if (prng.nextDouble() < config.askOrderProb) {
      val price = askPrice(prng, config.minAskPrice, config.maxAskPrice)
      val quantity = askQuantity(prng, config.minAskQuantity, config.maxAskQuantity)
      LimitAskOrder(self, price, quantity, timestamp(), tradable, uuid())
    } else {
      val price = bidPrice(prng, config.minBidPrice, config.maxBidPrice)
      val quantity = bidQuantity(prng, config.minBidQuantity, config.maxBidQuantity)
      LimitBidOrder(self, price, quantity, timestamp(), tradable, uuid())
    }
  }

  def submitLimitOrder(): Unit = {
    val (tradable, (market, ticker)) = prng.shuffle(markets).head
    market ! generateLimitOrder(tradable, ticker)
  }

}
