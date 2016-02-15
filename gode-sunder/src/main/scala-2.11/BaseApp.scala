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
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.agent.Agent

import java.io.PrintWriter

import markets.tickers.Tick
import play.api.libs.json.{Json, JsValue}

import scala.collection.{immutable}


trait BaseApp {

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
