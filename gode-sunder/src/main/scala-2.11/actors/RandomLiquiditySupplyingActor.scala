package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.RandomLiquiditySupplier
import markets.participants.strategies.RandomLimitOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.RandomOrderPlacementStrategy

import scala.collection.mutable


class RandomLiquiditySupplyingActor(val limitOrderTradingStrategy: RandomLimitOrderTradingStrategy,
                                    val markets: mutable.Map[Tradable, ActorRef],
                                    val orderPlacementStrategy: RandomOrderPlacementStrategy,
                                    val tickers: mutable.Map[Tradable, Agent[Tick]])
  extends RandomLiquiditySupplier[RandomLimitOrderTradingStrategy] {

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val initialDelay = orderPlacementStrategy.waitTime()
  val interval = orderPlacementStrategy.waitTime()
  context.system.scheduler.schedule(initialDelay, interval, self, SubmitLimitAskOrder)
  context.system.scheduler.schedule(initialDelay, interval, self, SubmitLimitBidOrder)

}


object RandomLiquiditySupplyingActor {

  def props(limitOrderTradingStrategy: RandomLimitOrderTradingStrategy,
            markets: mutable.Map[Tradable, ActorRef],
            orderPlacementStrategy: RandomOrderPlacementStrategy,
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new RandomLiquiditySupplyingActor(limitOrderTradingStrategy, markets, orderPlacementStrategy, tickers))
  }

}
