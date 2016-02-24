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

import actors.{ZILiquiditySupplier, ZILiquiditySupplierConfig}
import markets.tradables.Tradable

import scala.collection.immutable
import scala.concurrent.duration.Duration


object ZeroIntelligenceApp extends App with BaseApp {

  // Create some traders
  val numberTraders = config.getInt("traders.number")
  val tradersConfig = ZILiquiditySupplierConfig(config.getConfig("traders.params"))
  val traders = immutable.Vector.fill(numberTraders) {
    val props = ZILiquiditySupplier.props(tradersConfig, markets, prng, tickers)
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
    tickers.foreach {
      case (tradable, ticker) =>
        val jsonTicks = convertTicksToJson(ticker.get)
        val path = "./data/zero-intelligence/" + tradable.symbol + "" + ".json"
        writeTicksToFile(jsonTicks, path)
    }
  }(model.dispatcher)

}
