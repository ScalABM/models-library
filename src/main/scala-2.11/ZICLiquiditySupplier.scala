import akka.agent.Agent

import markets.orders.limit.{LimitBidOrder, LimitAskOrder, LimitOrderLike}
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.immutable


/** Zero Intelligence (Constrained) behavior as defined by Gode and Sunder, JPE (1993). */
trait ZICLiquiditySupplier extends LiquiditySupplier {
  this: RandomTradingActor =>

  def valuations: immutable.Map[Tradable, Long]

  def generateLimitOrder(tradable: Tradable, ticker: Agent[Tick]): LimitOrderLike = {
    val observedPrice = ticker().price.get  // @todo Tick price should not be optional!
    val valuation = valuations(tradable)
    if (observedPrice > valuation) {  // sell when tradable is over-valued!
      val price = askPrice(prng, valuation, config.maxAskPrice)
      val quantity = askQuantity(prng, config.minAskQuantity, config.maxAskQuantity)
      LimitAskOrder(self, price, quantity, timestamp(), tradable, uuid())
    } else {  // buy when tradable is under-valued!
      val price = bidPrice(prng, config.minBidPrice, valuation)
      val quantity = bidQuantity(prng, config.minBidQuantity, config.maxBidQuantity)
      LimitBidOrder(self, price, quantity, timestamp(), tradable, uuid())
    }
  }

  def submitLimitOrder(): Unit = {
    val (tradable, (market, ticker)) = prng.shuffle(markets).head
    market ! generateLimitOrder(tradable, ticker)
  }

}
