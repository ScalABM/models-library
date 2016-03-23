/*
 Copyright 2016 David R. Pugh

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

import akka.actor.{PoisonPill, ActorRef, Props}
import akka.agent.Agent

import actors.{LoggingSettlementMechanismActor, RandomLiquiditySupplyingActor}
import markets.MarketActor
import markets.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tickers.Tick
import markets.tradables.Tradable
import strategies.placement.{RandomOrderPlacementStrategyConfig, RandomOrderPlacementStrategy}
import strategies.trading.{ZILimitOrderTradingStrategy, ZILimitOrderTradingStrategyConfig}

import scala.collection.{mutable, immutable}
import scala.concurrent.duration.Duration


object ZeroIntelligenceApp extends App with BaseApp {

  // Create a simple settlement mechanism
  val settlementProps = Props[LoggingSettlementMechanismActor]
  val settlementMechanism = model.actorOf(settlementProps, "settlement-mechanism")

  // Create a collection of tickers (one for each tradable security)
  val initialTick = Tick(50, 150, 100, 1, System.currentTimeMillis())
  val tickers = tradables.map {
    security => security -> Agent(initialTick)(model.dispatcher)
  } (collection.breakOut): mutable.Map[Tradable, Agent[Tick]]

  // Create a collection of markets (one for each tradable security)
  val referencePrice = config.getLong("markets.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)
  val markets = tradables.map { security =>
    val props = MarketActor.props(matchingEngine, settlementMechanism, tickers(security), security)
    security -> model.actorOf(props.withDispatcher("markets.dispatcher"))
  } (collection.breakOut): mutable.Map[Tradable, ActorRef]

  // Create some traders
  val numberTraders = config.getInt("traders.number")
  val traders = immutable.Vector.fill(numberTraders) {

    // specify order placement strategy
    val orderPlacementStrategyConfig = new RandomOrderPlacementStrategyConfig(config.getConfig("traders.strategies.placement"))
    val orderPlacementStrategy = RandomOrderPlacementStrategy(orderPlacementStrategyConfig, prng)

    // specify trading strategy
    val tradingStrategyConfig = new ZILimitOrderTradingStrategyConfig(config.getConfig("traders.strategies.trading"))
    val tradingStrategy = ZILimitOrderTradingStrategy(tradingStrategyConfig, prng)

    // create a new actor
    val props = RandomLiquiditySupplyingActor.props(tradingStrategy, markets,
      orderPlacementStrategy, tickers)
    model.actorOf(props.withDispatcher("traders.dispatcher"))
  }

  // Initialize the Reaper
  val reaper = model.actorOf(Props[Reaper])
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  // terminate the simulation
  val length = config.getLong("simulation.duration.length")
  val unit = config.getString("simulation.duration.timeUnit")
  val simulationDuration = Duration(length, unit)
  model.scheduler.scheduleOnce(simulationDuration) {
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
  }(model.dispatcher)

}
