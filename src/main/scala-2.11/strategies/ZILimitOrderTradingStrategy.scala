package strategies

import akka.agent.Agent

import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable

/** Zero Intelligence (ZI) limit order trading strategy from Gode and Sunder, JPE (1996). */
trait ZILimitOrderTradingStrategy extends RandomLimitOrderTradingStrategy {

  def maxAskPrice: Long

  def maxAskQuantity: Long

  def maxBidPrice: Long

  def maxBidQuantity: Long

  def minAskPrice: Long

  def minAskQuantity: Long

  def minBidPrice: Long

  def minBidQuantity:Long

  def askPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(minAskPrice, maxAskPrice)
  }

  def askQuantity(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(minAskQuantity, maxAskQuantity)
  }

  def bidPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(minBidPrice, maxBidPrice)
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