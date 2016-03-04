package actors

import akka.actor.{ActorLogging, Actor}

import markets.Fill


class FillLoggingActor extends Actor with ActorLogging {

  def receive: Receive = {
    case fill: Fill =>
      log.info(fillToString(fill))
  }

  private[this] def fillToString(fill: Fill): String = {
    s"{askPrice:${fill.askOrder.price}, bidPrice:${fill.bidOrder.price}, price:${fill.price}, " +
      s"quantity:${fill.quantity}, timestamp:${fill.timestamp}"
  }
}
