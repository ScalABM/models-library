package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.LiquiditySupplier
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.PoissonOrderPlacementStrategy
import strategies.trading.PassiveLimitOrderTradingStrategy

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Random


case class PassiveLiquiditySupplier(config: RandomTraderConfig,
                                    markets: mutable.Map[Tradable, ActorRef],
                                    prng: Random,
                                    tickers: mutable.Map[Tradable, Agent[Tick]])
  extends LiquiditySupplier {

  val limitOrderTradingStrategy = PassiveLimitOrderTradingStrategy(config, prng)

  val orderPlacementStrategy = PoissonOrderPlacementStrategy(prng, context.system.scheduler)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  val initialDelay = Duration.Zero
  val limitOrderInterval = orderPlacementStrategy.waitTime(config.alpha)
  orderPlacementStrategy.schedule(initialDelay, limitOrderInterval, self, SubmitLimitAskOrder)
  orderPlacementStrategy.schedule(initialDelay, limitOrderInterval, self, SubmitLimitBidOrder)

}


object PassiveLiquiditySupplier {

  def props(config: RandomTraderConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new PassiveLiquiditySupplier(config, markets, prng, tickers))
  }

}
