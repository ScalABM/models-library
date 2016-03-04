package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.RandomLiquidityDemander
import markets.participants.strategies.RandomMarketOrderTradingStrategy
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.RandomOrderPlacementStrategy

import scala.collection.mutable


class RandomLiquidityDemandingActor(val marketOrderTradingStrategy: RandomMarketOrderTradingStrategy,
                                    val markets: mutable.Map[Tradable, ActorRef],
                                    val orderPlacementStrategy: RandomOrderPlacementStrategy,
                                    val tickers: mutable.Map[Tradable, Agent[Tick]])
  extends RandomLiquidityDemander[RandomMarketOrderTradingStrategy] {

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  import context.dispatcher
  val initialDelay = orderPlacementStrategy.waitTime()
  val interval = orderPlacementStrategy.waitTime()
  context.system.scheduler.schedule(initialDelay, interval, self, SubmitMarketAskOrder)
  context.system.scheduler.schedule(initialDelay, interval, self, SubmitMarketBidOrder)

}


object RandomLiquidityDemandingActor {

  def props(marketOrderTradingStrategy: RandomMarketOrderTradingStrategy,
            markets: mutable.Map[Tradable, ActorRef],
            orderPlacementStrategy: RandomOrderPlacementStrategy,
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new RandomLiquidityDemandingActor(marketOrderTradingStrategy, markets, orderPlacementStrategy, tickers))
  }

}
