package actors

import akka.actor.ActorLogging

import markets.Fill
import markets.settlement.SettlementMechanismActor
import play.api.libs.json.{JsObject, JsNumber, JsString, JsValue, Json}


class LoggingSettlementMechanismActor extends SettlementMechanismActor with ActorLogging {

  override def receive: Receive = {
    case fill: Fill =>
      val jsonFill = convertToJson(fill)
      log.info(jsonFill.toString())
      super.receive(fill)
  }

  private[this] def fillToString(fill: Fill): String = {
    s"""{"class":"${fill.getClass.getName}","tradable":"${fill.askOrder.tradable.symbol}",""" +
      s""""askPrice":${fill.askOrder.price},"bidPrice":${fill.bidOrder.price},""" +
      s""""price":${fill.price},"quantity":${fill.quantity},"timestamp":${fill.timestamp}}"""
  }

  /** Converts a Fill to JSON.
    *
    * @param fill
    * @return JSON data representing the Fill.
    */
  private[this] def convertToJson(fill: Fill): JsValue = {
    val fillAsList = Seq(
      "class" -> JsString(fill.getClass.getName),
      "tradable" -> JsString(fill.askOrder.tradable.symbol),
      "askPrice" -> JsNumber(fill.askOrder.price),
      "bidPrice" -> JsNumber(fill.bidOrder.price),
      "price" -> JsNumber(fill.price),
      "quantity" -> JsNumber(fill.quantity),
      "timestamp" -> JsNumber(fill.timestamp)
    )
    JsObject(fillAsList)
  }

}
