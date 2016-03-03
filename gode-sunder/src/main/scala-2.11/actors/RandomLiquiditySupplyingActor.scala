package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.RandomLiquiditySupplier
import markets.participants.strategies.RandomLimitOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.util.Random


case class RandomLiquiditySupplyingActor(limitOrderTradingStrategy: RandomLimitOrderTradingStrategy,
                                         markets: mutable.Map[Tradable, ActorRef],
                                         tickers: mutable.Map[Tradable, Agent[Tick]])
  extends RandomLiquiditySupplier {

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


object RandomLiquiditySupplingActor {

  def props(markets: mutable.Map[Tradable, ActorRef],
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new ZILiquiditySupplier(config, markets, prng, tickers))
  }

}
