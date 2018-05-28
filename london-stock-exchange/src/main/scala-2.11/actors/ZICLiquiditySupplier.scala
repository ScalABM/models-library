package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.RandomLiquiditySupplier
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.trading.ZICLimitOrderTradingStrategy

import scala.collection.{immutable, mutable}
import scala.concurrent.duration.FiniteDuration
import scala.util.Random


case class ZICLiquiditySupplier(config: ZILiquiditySupplierConfig,
                                markets: mutable.Map[Tradable, ActorRef],
                                prng: Random,
                                tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]],
                                valuations: mutable.Map[Tradable, Long])
  extends RandomLiquiditySupplier[ZICLimitOrderTradingStrategy] {

  val limitOrderTradingStrategy = ZICLimitOrderTradingStrategy(config, prng, valuations)

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


object ZICLiquiditySupplier {

  def props(config: ZILiquiditySupplierConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]],
            valuations: mutable.Map[Tradable, Long]): Props = {
    Props(new ZICLiquiditySupplier(config, markets, prng, tickers, valuations))
  }

}
