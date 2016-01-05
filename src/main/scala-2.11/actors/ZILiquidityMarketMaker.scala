package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.orders.Order
import markets.participants.LiquidityMarketMaker
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.RandomOrderPlacementStrategy
import strategies.trading.{ZIMarketOrderTradingStrategy, ZILimitOrderTradingStrategy}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random


case class ZILiquidityMarketMaker(config: RandomTraderConfig,
                                  markets: mutable.Map[Tradable, ActorRef],
                                  orderPlacementStrategy: RandomOrderPlacementStrategy,
                                  prng: Random,
                                  tickers: mutable.Map[Tradable, Agent[Tick]])
  extends LiquidityMarketMaker {

  val limitOrderTradingStrategy = ZILimitOrderTradingStrategy(config, prng)

  val marketOrderTradingStrategy = ZIMarketOrderTradingStrategy(config, prng)

  val outstandingOrders = mutable.Set.empty[Order]

  // possible insert this into post-start life-cycle hook?
  orderPlacementStrategy.schedule(self, SubmitLimitAskOrder)
  orderPlacementStrategy.schedule(self, SubmitLimitBidOrder)
  orderPlacementStrategy.schedule(self, SubmitMarketAskOrder)
  orderPlacementStrategy.schedule(self, SubmitMarketBidOrder)

}


object ZILiquidityMarketMaker {

  def props(config: RandomTraderConfig,
            markets: mutable.Map[Tradable, ActorRef],
            orderPlacementStrategy: RandomOrderPlacementStrategy,
            prng: Random,
            tickers: mutable.Map[Tradable, Agent[Tick]]): Props = {
    Props(new ZILiquidityMarketMaker(config, markets, orderPlacementStrategy, prng, tickers))
  }

}
