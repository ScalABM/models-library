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
import akka.actor.{PoisonPill, ActorRef, ActorSystem}
import akka.agent.Agent

import java.io.PrintWriter

import Reaper.WatchMe
import actors.{RandomTraderConfig, ZILiquiditySupplier}
import com.typesafe.config.ConfigFactory
import markets.MarketActor
import markets.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tickers.Tick
import markets.tradables.{Security, Tradable}
import play.api.libs.json.{Json, JsValue}

import scala.collection.{mutable, immutable}
import scala.concurrent.duration._
import scala.util.Random


object GodeSunderModel extends App {

  def convertTicksToJson(ticks: immutable.Seq[Tick]): JsValue = {
    Json.toJson(
      ticks.map { tick => immutable.Map("askPrice" -> tick.askPrice, "bidPrice" -> tick.bidPrice,
        "price" -> tick.price, "quantity" -> tick.quantity, "timestamp" -> tick.timestamp)
      })
  }

  def writeTicksToFile(json: JsValue, path: String): Unit = {
    val target = new PrintWriter(path)
    target.write(json.toString())
    target.close()
  }

  val config = ConfigFactory.load("godeSunderModel.conf")

  // set the seed
  val prng = new Random(42)

  // Create some tradable tradables...
  val numberTradables = config.getInt("markets.number")
  val tradables = immutable.Seq.fill[Tradable](numberTradables){
    Security(prng.alphanumeric.take(4).mkString)
  }

  // Create some tickers...
  val model = ActorSystem("model", config)
  import model.dispatcher

  val counter = Agent[Int](0)

  val numberRoutees = config.getInt("markets.settlement.numberRoutees")
  val settlementMechanism = model.actorOf(SettlementRouter.props(counter, numberRoutees))

  // create some tickers
  val tickers = tradables.map {
    security => security -> Agent(immutable.Seq.empty[Tick])
  } (collection.breakOut): mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]

  // Create some markets
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

  // Initialize the reaper
  val reaper = model.actorOf(ProductionReaper.props(counter))
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  model.scheduler.scheduleOnce(1.minute) {
    tickers.foreach {
      case (tradable, ticker) =>
        val jsonTicks = convertTicksToJson(ticker.get)
        writeTicksToFile(jsonTicks, "./data/gode-sunder-model/" + tradable.symbol + ".json")
    }
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
    println(counter.get)
  }
}
