package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.RandomLiquidityMarketMaker
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.trading.{ZIMarketOrderTradingStrategy, ZILimitOrderTradingStrategy}

import scala.collection.mutable
import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.util.Random


case class ZILiquidityMarketMaker(config: ZILiquidityMarketMakerConfig,
                                  markets: mutable.Map[Tradable, ActorRef],
                                  prng: Random,
                                  tickers: mutable.Map[Tradable, Agent[Tick]])
  extends RandomLiquidityMarketMaker[ZILimitOrderTradingStrategy, ZIMarketOrderTradingStrategy] {

  val limitOrderTradingStrategy = ZILimitOrderTradingStrategy(config, prng)

  val marketOrderTradingStrategy = ZIMarketOrderTradingStrategy(config, prng)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val initialDelay = Duration.Zero
  val limitOrderInterval = waitTime(config.alpha)
  val marketOrderInterval = waitTime(config.mu)

  context.system.scheduler.schedule(initialDelay, limitOrderInterval, self, SubmitLimitAskOrder)
  context.system.scheduler.schedule(initialDelay, limitOrderInterval, self, SubmitLimitBidOrder)
  context.system.scheduler.schedule(initialDelay, marketOrderInterval, self, SubmitMarketAskOrder)
  context.system.scheduler.schedule(initialDelay, marketOrderInterval, self, SubmitMarketBidOrder)

  private[this] def waitTime(rate: Double, unit: String): FiniteDuration = {
    FiniteDuration((-Math.log(prng.nextDouble()) / rate).toLong, unit)
  }
}


object ZILiquidityMarketMaker {

  def props(config: ZILiquidityMarketMakerConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new ZILiquidityMarketMaker(config, markets, prng, tickers))
  }

}
