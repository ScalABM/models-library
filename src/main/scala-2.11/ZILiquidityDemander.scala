import akka.agent.Agent

import markets.orders.market.{MarketBidOrder, MarketAskOrder, MarketOrderLike}
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.util.Random


/** Zero Intelligence (ZI) liquidity demander.
  *
  * @note A ZI liquidity demander randomly generates `MarketOrderLike` orders.
  */
trait ZILiquidityDemander extends LiquidityDemander {
  this: RandomTradingActor =>

  def askQuantity(prng: Random, lower: Long, upper: Long): Long

  def bidQuantity(prng: Random, lower: Long, upper: Long): Long

  def generateMarketOrder(tradable: Tradable, ticker: Agent[Tick]): MarketOrderLike = {
    if (prng.nextDouble() < config.askOrderProb) {
      val quantity = askQuantity(prng, config.minAskQuantity, config.maxAskQuantity)
      MarketAskOrder(self, quantity, timestamp(), tradable, uuid())
    } else {
      val quantity = bidQuantity(prng, config.minAskQuantity, config.maxAskQuantity)
      MarketBidOrder(self, quantity, timestamp(), tradable, uuid())
    }
  }

  def submitMarketOrder(): Unit = {
    val (tradable, (market, ticker)) = prng.shuffle(markets).head
    market ! generateMarketOrder(tradable, ticker)
  }

}
