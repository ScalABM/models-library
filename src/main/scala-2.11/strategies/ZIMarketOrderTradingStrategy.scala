package strategies

import akka.agent.Agent

import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable

/** Zero Intelligence (ZI) market order trading strategy from Gode and Sunder, JPE (1996). */
trait ZIMarketOrderTradingStrategy extends RandomLimitOrderTradingStrategy {

  def maxAskQuantity: Long

  def maxBidQuantity: Long

  def minAskQuantity: Long

  def minBidQuantity:Long

  def askQuantity(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(minAskQuantity, maxAskQuantity)
  }

  def bidQuantity(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(minBidQuantity, maxBidQuantity)
  }

  def chooseOneOf(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Tradable, Agent[Tick])] = {
    if (tickers.isEmpty) None else Some(tickers.toIndexedSeq(prng.nextInt(tickers.size)))
  }

  protected def uniformRandomVariate(lower: Long, upper: Long): Long = {
    (lower + (upper - lower) * prng.nextDouble()).toLong
  }

}