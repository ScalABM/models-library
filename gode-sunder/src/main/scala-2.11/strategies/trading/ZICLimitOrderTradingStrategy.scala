package strategies.trading

import akka.agent.Agent

import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable
import scala.util.Random


/** Zero Intelligence (Constrained) behavior as defined by Gode and Sunder, JPE (1993). */
class ZICLimitOrderTradingStrategy(config: ZILimitOrderTradingStrategyConfig,
                                   prng: Random,
                                   valuations: mutable.Map[Tradable, Long])
  extends ZILimitOrderTradingStrategy(config, prng) {

  override def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Long,
    Tradable)] = {
    chooseOneOf(overValued(tickers)) match {
      case Some((tradable, ticker)) =>
        Some(askPrice(ticker, tradable), askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  override def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Long,
    Tradable)] = {
    chooseOneOf(underValued(tickers)) match {
      case Some((tradable, ticker)) =>
        Some(bidPrice(ticker, tradable), bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  private[this] def overValued(tickers: mutable.Map[Tradable, Agent[Tick]]) = {
    tickers.filter {
      case (tradable, ticker) => valuations(tradable) <= ticker.get.price
    }
  }

  private[this] def underValued(tickers: mutable.Map[Tradable, Agent[Tick]]) = {
    tickers.filter {
      case (tradable, ticker) => valuations(tradable) >= ticker.get.price
    }
  }

}


object ZICLimitOrderTradingStrategy {

  def apply(config: ZILimitOrderTradingStrategyConfig,
            prng: Random,
            valuations: mutable.Map[Tradable, Long]): ZICLimitOrderTradingStrategy = {
    new ZICLimitOrderTradingStrategy(config, prng, valuations)
  }

}