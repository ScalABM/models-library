import akka.actor.{Props, Actor}
import akka.agent.Agent
import akka.routing.{SmallestMailboxRoutingLogic, Router, ActorRefRoutee}

import markets.Fill

import scala.collection.immutable


class SettlementRouter(counter: Agent[Int], numberRoutees: Int) extends Actor {

  var router = {
    val routees = immutable.IndexedSeq.fill(numberRoutees) {
      ActorRefRoutee(context.actorOf(FillCountingActor.props(counter)))
    }
    Router(SmallestMailboxRoutingLogic(), routees)
  }

  def receive: Receive = {
    case message: Fill =>
      router.route(message, sender())
  }

}


object SettlementRouter {

  def props(counter: Agent[Int], numberRoutees: Int): Props = {
    Props(new SettlementRouter(counter, numberRoutees))
  }

}
