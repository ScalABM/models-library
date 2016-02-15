package strategies.trading

import akka.agent.Agent

import actors.ZILiquiditySupplierConfig
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.{immutable, mutable}
import scala.util.Random


/** Zero Intelligence (ZI) limit order trading strategy from Gode and Sunder, JPE (1996). */
class ZILimitOrderTradingStrategy(val config: ZILiquiditySupplierConfig, val prng: Random)
  extends RandomLimitOrderTradingStrategy {

  def askPrice(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskPrice, config.maxAskPrice)
  }

  def askQuantity(ticker: Agent[immutable.Seq[Tick]], tradable: Tradable): Long = {
    uniformRandomVariate(config.minAskQuantity, config.maxAskQuantity)
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


object ZILimitOrderTradingStrategy {

  def apply(config: ZILiquiditySupplierConfig, prng: Random): ZILimitOrderTradingStrategy = {
    new ZILimitOrderTradingStrategy(config, prng)
  }

}