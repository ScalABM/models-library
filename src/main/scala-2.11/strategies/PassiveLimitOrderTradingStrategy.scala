package strategies

import akka.agent.Agent

import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable

/** Passive limit order trading strategy from Farmer et al, PNAS (2005).
  *
  * @note Liquidity supplier behavior is "passive" because actor observes most recent ask and bid
  *       prices and then places its next limit order in an attempt to insure that the order
  *       rests in the book (rather than executing immediately).
  */
trait PassiveLimitOrderTradingStrategy extends ZILimitOrderTradingStrategy {

  override def askPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(ticker.get.bidPrice, maxAskPrice)
  }

  override def bidPrice(ticker: Agent[Tick], tradable: Tradable): Long = {
    uniformRandomVariate(ticker.get.askPrice, maxBidPrice)
  }

}