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
    uniformRandomVariate(ticker.get.head.bidPrice, config.maxAskPrice)
  }

  override def bidPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(ticker.get.head.askPrice, config.maxBidPrice)
  }

}


object PassiveLimitOrderTradingStrategy {

  def apply(config: RandomTraderConfig, prng: Random): PassiveLimitOrderTradingStrategy = {
    new PassiveLimitOrderTradingStrategy(config, prng)
  }

}