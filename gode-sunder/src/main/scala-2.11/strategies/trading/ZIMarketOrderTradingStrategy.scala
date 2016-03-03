package strategies.trading

import akka.agent.Agent

import markets.participants.strategies.RandomMarketOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable
import scala.util.Random


/** Zero Intelligence (ZI) market order trading strategy from Gode and Sunder, JPE (1996). */
class ZIMarketOrderTradingStrategy(val config: ZITradingStrategyConfig, val prng: Random)
  extends RandomMarketOrderTradingStrategy with ZITradingStrategy {

  def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

}


object ZIMarketOrderTradingStrategy {

  def apply(config: ZITradingStrategyConfig, prng: Random): ZIMarketOrderTradingStrategy = {
    new ZIMarketOrderTradingStrategy(config, prng)
  }

}