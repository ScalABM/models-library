package strategies.trading

import akka.agent.Agent

import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable
import scala.util.Random


trait ZITradingStrategy {

  def config: ZITradingStrategyConfig

  def prng: Random

  def askQuantity(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskQuantity, config.maxAskQuantity)
  }

  def bidQuantity(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(config.minBidQuantity, config.maxBidQuantity)
  }

  def chooseOneOf(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Tradable, Agent[Tick])] = {
    if (tickers.isEmpty) None else Some(tickers.toIndexedSeq(prng.nextInt(tickers.size)))
  }

  protected def uniformRandomVariate(lower: Long, upper: Long): Long = {
    (lower + (upper - lower) * prng.nextDouble()).toLong
  }

}
