package strategies.trading

import akka.agent.Agent

import actors.ZILiquiditySupplierConfig
import markets.participants.strategies.RandomLimitOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.{immutable, mutable}
import scala.util.Random


/** Zero Intelligence (ZI) limit order trading strategy from Gode and Sunder, JPE (1996). */
case class ZILimitOrderTradingStrategy(config: ZILiquiditySupplierConfig, prng: Random)
  extends RandomLimitOrderTradingStrategy {

  def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(askPrice(ticker, tradable), askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def askPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskPrice, config.maxAskPrice)
  }

  def askQuantity(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskQuantity, config.maxAskQuantity)
  }

  def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(bidPrice(ticker, tradable), bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def bidPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minBidPrice, config.maxBidPrice)
  }

  def bidQuantity(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minBidQuantity, config.maxBidQuantity)
  }

  def chooseOneOf(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Tradable, Agent[immutable.Seq[Tick]])] = {
    if (tickers.isEmpty) None else Some(tickers.toIndexedSeq(prng.nextInt(tickers.size)))
  }

  protected def uniformRandomVariate(lower: Long, upper: Long): Long = {
    (lower + (upper - lower) * prng.nextDouble()).toLong
  }

}