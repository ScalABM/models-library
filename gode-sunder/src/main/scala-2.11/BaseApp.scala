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

import scala.collection.immutable
import scala.util.Random


trait BaseApp {

  def traders: immutable.IndexedSeq[ActorRef]

  val config = ConfigFactory.load("model.conf")

  val model = ActorSystem("gode-sunder-model", config)

  val seed = config.getLong("simulation.seed")
  val prng = new Random(seed)

  // Create some tradable Securities...
  val numberMarkets = config.getInt("markets.number")
  val tradables = immutable.Seq.fill[Tradable](numberMarkets){
    Security(prng.alphanumeric.take(4).mkString)
  }

}
