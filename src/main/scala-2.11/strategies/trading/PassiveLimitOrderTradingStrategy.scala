package strategies.trading

import akka.agent.Agent

import actors.RandomTraderConfig
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.immutable
import scala.util.Random

/** Passive limit order trading strategy from Farmer et al, PNAS (2005).
  *
  * @note Liquidity supplier behavior is "passive" because actor observes most recent ask and bid
  *       prices and then places its next limit order in an attempt to insure that the order
  *       rests in the book (rather than executing immediately).
  */
class PassiveLimitOrderTradingStrategy(config: RandomTraderConfig, prng: Random)
  extends ZILimitOrderTradingStrategy(config, prng) {

  override def askPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    val minAskPrice = ticker.get.headOption match {
      case Some(tick) => tick.price
      case None => config.minAskPrice
    }
    logUniformRandomVariate(math.log(minAskPrice), math.log(config.maxAskPrice))
  }

  override def bidPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    val maxBidPrice = ticker.get.headOption match {
      case Some(tick) => tick.price
      case None => config.maxBidPrice
    }
    logUniformRandomVariate(math.log(config.minBidPrice), math.log(maxBidPrice))
  }

  protected def logUniformRandomVariate(lower: Double, upper: Double): Long = {
    math.exp(lower + (upper - lower) * prng.nextDouble()).toLong
  }

}


object PassiveLimitOrderTradingStrategy {

  def apply(config: RandomTraderConfig, prng: Random): PassiveLimitOrderTradingStrategy = {
    new PassiveLimitOrderTradingStrategy(config, prng)
  }

}