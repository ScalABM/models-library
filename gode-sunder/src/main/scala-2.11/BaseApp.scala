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
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.agent.Agent

import java.io.PrintWriter

import actors.SimpleSettlementMechanismActor
import com.typesafe.config.ConfigFactory
import markets.MarketActor
import markets.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tickers.Tick
import markets.tradables.{Security, Tradable}
import play.api.libs.json.{Json, JsValue}

import scala.collection.{mutable, immutable}
import scala.util.Random


trait BaseApp {

  def traders: immutable.IndexedSeq[ActorRef]

  val config = ConfigFactory.load("godeSunderModel.conf")

  val model = ActorSystem("gode-sunder-model", config)

  val seed = config.getLong("simulation.seed")
  val prng = new Random(seed)

  // Create some tradable Securities...
  val numberMarkets = config.getInt("markets.number")
  val tradables = immutable.Seq.fill[Tradable](numberMarkets){
    Security(prng.alphanumeric.take(4).mkString)
  }

  // Create a simple settlement mechanism
  val settlementProps = Props[SimpleSettlementMechanismActor]
  val settlementMechanism = model.actorOf(settlementProps, "settlement-mechanism")

  // Create a collection of tickers (one for each tradable security)
  val initialTick = Tick(50, 150, 100, 1, System.currentTimeMillis())
  val tickers = tradables.map {
    security => security -> Agent(immutable.Seq(initialTick))(model.dispatcher)
  } (collection.breakOut): mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]

  // Create a collection of markets (one for each tradable security)
  val referencePrice = config.getLong("markets.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)
  val markets = tradables.map { security =>
    val props = MarketActor.props(matchingEngine, settlementMechanism, tickers(security), security)
    security -> model.actorOf(props.withDispatcher("markets.dispatcher"))
  } (collection.breakOut): mutable.Map[Tradable, ActorRef]

  /** Converts a collection of Ticks to JSON.
    *
    * @param ticks the collection of Tick instances to be converted into JSON.
    * @return JSON data representing the collection of Tick instances.
    */
  def convertTicksToJson(ticks: immutable.Seq[Tick]): JsValue = {
    Json.toJson(
      ticks.map { tick => immutable.Map("askPrice" -> tick.askPrice, "bidPrice" -> tick.bidPrice,
        "price" -> tick.price, "quantity" -> tick.quantity, "timestamp" -> tick.timestamp)
      })
  }

  /** Writes JSON Ticks to a specified file.
    *
    * @param json
    * @param path
    */
  def writeTicksToFile(json: JsValue, path: String): Unit = {
    val target = new PrintWriter(path)
    target.write(json.toString())
    target.close()
  }
}
