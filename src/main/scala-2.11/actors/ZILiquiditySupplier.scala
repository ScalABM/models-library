package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.LiquiditySupplier
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.PoissonOrderPlacementStrategy
import strategies.trading.ZILimitOrderTradingStrategy

import scala.collection.{immutable, mutable}
import scala.concurrent.duration.Duration
import scala.util.Random


case class ZILiquiditySupplier(config: RandomTraderConfig,
                               markets: mutable.Map[Tradable, ActorRef],
                               prng: Random,
                               tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]])
  extends LiquiditySupplier {

  val limitOrderTradingStrategy = ZILimitOrderTradingStrategy(config, prng)

  val orderPlacementStrategy = PoissonOrderPlacementStrategy(prng, context.system.scheduler)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val initialDelay = Duration.Zero
  val limitOrderInterval = orderPlacementStrategy.waitTime(config.alpha)
  orderPlacementStrategy.schedule(initialDelay, limitOrderInterval, self, SubmitLimitAskOrder)
  orderPlacementStrategy.schedule(initialDelay, limitOrderInterval, self, SubmitLimitBidOrder)

}


object ZILiquiditySupplier {

  def props(config: RandomTraderConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Props = {
    Props(new ZILiquiditySupplier(config, markets, prng, tickers))
  }

}
