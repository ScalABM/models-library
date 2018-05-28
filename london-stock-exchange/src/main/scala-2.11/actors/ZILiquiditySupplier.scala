package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.RandomLiquiditySupplier
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.trading.ZILimitOrderTradingStrategy

import scala.collection.{immutable, mutable}
import scala.concurrent.duration.FiniteDuration
import scala.util.Random


case class ZILiquiditySupplier(config: ZILiquiditySupplierConfig,
                               markets: mutable.Map[Tradable, ActorRef],
                               prng: Random,
                               tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]])
  extends RandomLiquiditySupplier[ZILimitOrderTradingStrategy] {

  val limitOrderTradingStrategy = ZILimitOrderTradingStrategy(config, prng)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val interval = waitTime(config.alpha, config.timeUnit)
  context.system.scheduler.schedule(interval, interval, self, SubmitLimitAskOrder)
  context.system.scheduler.schedule(interval, interval, self, SubmitLimitBidOrder)

  private[this] def waitTime(rate: Double, unit: String): FiniteDuration = {
    FiniteDuration((-Math.log(prng.nextDouble()) / rate).toLong, unit)
  }

}


object ZILiquiditySupplier {

  def props(config: ZILiquiditySupplierConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Props = {
    Props(new ZILiquiditySupplier(config, markets, prng, tickers))
  }

}
