package strategies.trading

import akka.agent.Agent
import markets.participants.strategies.RandomLimitOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.{immutable, mutable}
import scala.util.Random


/** Zero Intelligence (ZI) limit order trading strategy from Gode and Sunder, JPE (1996). */
class ZILimitOrderTradingStrategy(val config: ZILimitOrderTradingStrategyConfig, val prng: Random)
  extends RandomLimitOrderTradingStrategy with ZITradingStrategy {

  def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(askPrice(ticker, tradable), askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def askPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskPrice, config.maxAskPrice)
  }

  def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(bidPrice(ticker, tradable), bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def bidPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(config.minBidPrice, config.maxBidPrice)
  }

}


object ZILimitOrderTradingStrategy {

  def apply(config: ZILimitOrderTradingStrategyConfig,
            prng: Random): ZILimitOrderTradingStrategy = {
    new ZILimitOrderTradingStrategy(config, prng)
  }

}