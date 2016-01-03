package strategies

import akka.agent.Agent

import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable


/** Zero Intelligence (Constrained) behavior as defined by Gode and Sunder, JPE (1993). */
trait ZICLimitOrderTradingStrategy extends ZILimitOrderTradingStrategy {

  def valuations: mutable.Map[Tradable, Long]

  override def askPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(valuations(tradable), maxAskPrice)
  }

  override def bidPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(minBidPrice, valuations(tradable))
  }

  override def askOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Long, Tradable)] = {
    val overValuedTickers = tickers.filter {
      case (tradable, ticker) => valuations(tradable) <= ticker.get.price.get
    }
    chooseOneOf(overValuedTickers) match {
      case Some((tradable, ticker)) =>
        Some(askPrice(ticker, tradable), askQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

  override def bidOrderStrategy(tickers: mutable.Map[Tradable, Agent[Tick]]): Option[(Long, Long, Tradable)] = {
    val underValuedTickers = tickers.filter {
      case (tradable, ticker) => valuations(tradable) >= ticker.get.price.get
    }
    chooseOneOf(underValuedTickers) match {
      case Some((tradable, ticker)) =>
        Some(bidPrice(ticker, tradable), bidQuantity(ticker, tradable), tradable)
      case None =>
        None
    }
  }

}
