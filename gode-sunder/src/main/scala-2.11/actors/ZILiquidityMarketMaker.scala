package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.{OrderCanceler, LiquidityMarketMaker}
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.cancellation.RandomOrderCancellationStrategy
import strategies.placement.PoissonOrderPlacementStrategy
import strategies.trading.{ZIMarketOrderTradingStrategy, ZILimitOrderTradingStrategy}

import scala.collection.{immutable, mutable}
import scala.concurrent.duration.Duration
import scala.util.Random


case class ZILiquidityMarketMaker(config: ZILiquidityMarketMakerConfig,
                                  markets: mutable.Map[Tradable, ActorRef],
                                  prng: Random,
                                  tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]])
  extends LiquidityMarketMaker with OrderCanceler {

  val limitOrderTradingStrategy = ZILimitOrderTradingStrategy(config, prng)

  val marketOrderTradingStrategy = ZIMarketOrderTradingStrategy(config, prng)

  val orderCancellationStrategy = RandomOrderCancellationStrategy(prng)

  val orderPlacementStrategy = PoissonOrderPlacementStrategy(prng, context.system.scheduler)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val initialDelay = Duration.Zero
  val cancellationInterval = orderPlacementStrategy.waitTime(config.delta)
  val limitOrderInterval = orderPlacementStrategy.waitTime(config.alpha)
  val marketOrderInterval = orderPlacementStrategy.waitTime(config.mu)

  orderPlacementStrategy.schedule(initialDelay, cancellationInterval, self, SubmitOrderCancellation)
  orderPlacementStrategy.schedule(initialDelay, limitOrderInterval, self, SubmitLimitAskOrder)
  orderPlacementStrategy.schedule(initialDelay, limitOrderInterval, self, SubmitLimitBidOrder)
  orderPlacementStrategy.schedule(initialDelay, marketOrderInterval, self, SubmitMarketAskOrder)
  orderPlacementStrategy.schedule(initialDelay, marketOrderInterval, self, SubmitMarketBidOrder)

}


object ZILiquidityMarketMaker {

  def props(config: ZILiquidityMarketMakerConfig,
            markets: mutable.Map[Tradable, ActorRef],
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]): Props = {
    Props(new ZILiquidityMarketMaker(config, markets, prng, tickers))
  }

}
