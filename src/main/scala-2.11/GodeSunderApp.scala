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
import akka.actor.{Props, PoisonPill, ActorRef, ActorSystem}
import akka.agent.Agent

import actors.{SimpleSettlementMechanismActor, RandomTraderConfig, ZILiquiditySupplier}
import com.typesafe.config.ConfigFactory
import markets.MarketActor
import markets.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tickers.Tick
import markets.tradables.{Security, Tradable}

import scala.collection.{mutable, immutable}
import scala.concurrent.duration._
import scala.util.Random


object GodeSunderApp extends App with BaseApp {

  val config = ConfigFactory.load("godeSunderModel.conf")

  val model = ActorSystem("gode-sunder-model", config)

  val path: String = "./data/gode-sunder-model/"

  val prng = new Random(42)

  // Create some tradable Securities...
  val numberMarkets = config.getInt("markets.number")
  val tradables = immutable.Seq.fill[Tradable](numberMarkets){
    Security(prng.alphanumeric.take(4).mkString)
  }

  // Create a simple settlement mechanism
  val settlementProps = Props[SimpleSettlementMechanismActor]
  val settlementMechanism = model.actorOf(settlementProps, "settlement-mechanism")

  // Create a collection of tickers (one for each tradable security)
  val tickers = tradables.map {
    security => security -> Agent(immutable.Seq.empty[Tick])(model.dispatcher)
  } (collection.breakOut): mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]

  // Create a collection of markets (one for each tradable security)
  val referencePrice = config.getLong("markets.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)
  val markets = tradables.map { security =>
    val props = MarketActor.props(matchingEngine, settlementMechanism, tickers(security), security)
    security -> model.actorOf(props.withDispatcher("markets.dispatcher"))
  } (collection.breakOut): mutable.Map[Tradable, ActorRef]

  // Create some traders
  val numberTraders = config.getInt("traders.number")
  val traderConfig = new RandomTraderConfig(config.getConfig("traders.params"))
  val traders = immutable.IndexedSeq.fill(numberTraders) {
    model.actorOf(ZILiquiditySupplier.props(traderConfig, markets, prng, tickers))
  }

  // Initialize the Reaper
  val reaper = model.actorOf(Props[Reaper])
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  model.scheduler.scheduleOnce(1.minute) {
    tickers.foreach {
      case (tradable, ticker) =>
        val jsonTicks = convertTicksToJson(ticker.get)
        writeTicksToFile(jsonTicks, path + tradable.symbol + ".json")
    }
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
  }(model.dispatcher)

}
