package strategies.trading

import akka.agent.Agent

import actors.ZILiquiditySupplierConfig
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.{immutable, mutable}
import scala.util.Random


/** Zero Intelligence (Constrained) behavior as defined by Gode and Sunder, JPE (1993). */
class ZICLimitOrderTradingStrategy(config: ZILiquiditySupplierConfig,
                                   prng: Random,
                                   valuations: mutable.Map[Tradable, Long])
  extends ZILimitOrderTradingStrategy(config, prng) {

  override def askPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(valuations(tradable), config.maxAskPrice)
  }

  override def bidPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minBidPrice, valuations(tradable))
  }

  override def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Long, Tradable)] = {
    val overValuedTickers = tickers.filter {
      case (tradable, ticker) =>
        valuations(tradable) <= ticker.get.head.price
    }
    chooseOneOf(overValuedTickers) match {
      case Some((tradable, ticker)) =>
        Some(askPrice(ticker, tradable), askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  override def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Long, Tradable)] = {
    val underValuedTickers = tickers.filter {
      case (tradable, ticker) =>
        valuations(tradable) >= ticker.get.head.price
    }
    chooseOneOf(underValuedTickers) match {
      case Some((tradable, ticker)) =>
        Some(bidPrice(ticker, tradable), bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

}
