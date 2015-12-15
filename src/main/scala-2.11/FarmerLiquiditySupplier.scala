import akka.agent.Agent

import markets.orders.limit.{LimitBidOrder, LimitAskOrder, LimitOrderLike}
import markets.tickers.Tick
import markets.tradables.Tradable

/** Zero Intelligence Plus liquidity supplier behavior as defined by Farmer et al, PNAS (2005). */
trait FarmerLiquiditySupplier extends LiquiditySupplier {
  this: RandomTradingActor =>

  def generateLimitOrder(tradable: Tradable, ticker: Agent[Tick]): LimitOrderLike = {
    if (prng.nextDouble() < config.askOrderProb) {
      val price = askPrice(prng, ticker().bidPrice, config.maxAskPrice)
      val quantity = askQuantity(prng, config.minAskQuantity, config.maxAskQuantity)
      LimitAskOrder(self, price, quantity, timestamp(), tradable, uuid())
    } else {
      val price = bidPrice(prng, config.minBidPrice, ticker().askPrice)
      val quantity = bidQuantity(prng, config.minBidQuantity, config.maxBidQuantity)
      LimitBidOrder(self, price, quantity, timestamp(), tradable, uuid())
    }
  }

  def submitLimitOrder(): Unit = {
    val (tradable, (market, ticker)) = prng.shuffle(markets).head
    market ! generateLimitOrder(tradable, ticker)
  }

}