package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.LiquiditySupplier
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.RandomOrderPlacementStrategy
import strategies.trading.PassiveLimitOrderTradingStrategy

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random


case class PassiveLiquiditySupplier(config: RandomTraderConfig,
                                    markets: mutable.Map[Tradable, ActorRef],
                                    orderPlacementStrategy: RandomOrderPlacementStrategy,
                                    prng: Random,
                                    tickers: mutable.Map[Tradable, Agent[Tick]])
  extends LiquiditySupplier {

  val limitOrderTradingStrategy = PassiveLimitOrderTradingStrategy(config, prng)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  orderPlacementStrategy.schedule(self, SubmitLimitAskOrder)
  orderPlacementStrategy.schedule(self, SubmitLimitBidOrder)

}


object PassiveLiquiditySupplier {

  def props(config: RandomTraderConfig,
            markets: mutable.Map[Tradable, ActorRef],
            orderPlacementStrategy: RandomOrderPlacementStrategy,
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new PassiveLiquiditySupplier(config, markets, orderPlacementStrategy, prng, tickers))
  }

}
