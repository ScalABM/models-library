package strategies.trading

import akka.agent.Agent

import markets.participants.strategies.MarketOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.{immutable, mutable}
import scala.util.Random


trait RandomMarketOrderTradingStrategy extends MarketOrderTradingStrategy {

  def askQuantity(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long

  def bidQuantity(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long

  def chooseOneOf(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Tradable, Agent[immutable.Seq[Tick]])]

  def prng: Random

  def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Option[(Long, Tradable)] = {
    chooseOneOf(tickers) match {
      case Some((tradable, ticker)) =>
        Some(bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

}
