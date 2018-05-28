package strategies.trading

import akka.agent.Agent

import actors.ZILiquiditySupplierConfig
import markets.participants.strategies.RandomLimitOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.{immutable, mutable}
import scala.util.Random


/** Zero Intelligence (Constrained) behavior as defined by Gode and Sunder, JPE (1993). */
case class ZICLimitOrderTradingStrategy(config: ZILiquiditySupplierConfig,
                                        prng: Random,
                                        valuations: mutable.Map[Tradable, Long])
  extends RandomLimitOrderTradingStrategy {

  def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Long, Tradable)] = {
    chooseOneOf(overValued(tickers)) match {
      case Some((tradable, ticker)) =>
        Some(askPrice(ticker, tradable), askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def askPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(valuations(tradable), config.maxAskPrice)
  }

  def askQuantity(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskQuantity, config.maxAskQuantity)
  }

  def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Long, Tradable)] = {
    chooseOneOf(underValued(tickers)) match {
      case Some((tradable, ticker)) =>
        Some(bidPrice(ticker, tradable), bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def bidPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minBidPrice, valuations(tradable))
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

  private[this] def overValued(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]) = {
    tickers.filter {
      case (tradable, ticker) => valuations(tradable) <= ticker.get.head.price
    }
  }

  private[this] def underValued(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]) = {
    tickers.filter {
      case (tradable, ticker) => valuations(tradable) >= ticker.get.head.price
    }
  }

}
