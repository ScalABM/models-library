package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.{RandomLiquidityDemander, LiquidityDemander}
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.trading.ZIMarketOrderTradingStrategy

import scala.collection.{immutable, mutable}
import scala.concurrent.duration.FiniteDuration
import scala.util.Random


case class ZILiquidityDemander(config: RandomLiquidityDemanderConfig,
                               markets: mutable.Map[Tradable, ActorRef],
                               prng: Random,
                               tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]])
  extends RandomLiquidityDemander[ZIMarketOrderTradingStrategy] {

  val marketOrderTradingStrategy = ZIMarketOrderTradingStrategy(config, prng)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val interval = waitTime(config.mu, config.timeUnit)
  context.system.scheduler.schedule(interval, interval, self, SubmitMarketAskOrder)
  context.system.scheduler.schedule(interval, interval, self, SubmitMarketBidOrder)

  private[this] def waitTime(rate: Double, unit: String): FiniteDuration = {
    FiniteDuration((-Math.log(prng.nextDouble()) / rate).toLong, unit)
  }

}


object ZILiquidityDemander {

  def props(config: RandomLiquidityDemanderConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Props = {
    Props(new ZILiquidityDemander(config, markets, prng, tickers))
  }

}
