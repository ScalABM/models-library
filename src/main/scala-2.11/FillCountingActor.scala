import akka.actor.Props
import akka.agent.Agent

import markets.{Filled, Fill, StackableActor}


class FillCountingActor(counter: Agent[Int]) extends StackableActor {

  wrappedBecome(fillCountingBehavior)

  def fillCountingBehavior: Receive = {
    case fill @ Fill(ask, bid, _, _, residualAsk, residualBid, _, _) =>
      ask.issuer ! Filled(ask, residualAsk, timestamp(), uuid())
      bid.issuer ! Filled(bid, residualBid, timestamp(), uuid())
      counter send (_ + 1)
  }
}


object FillCountingActor {

  def props(counter: Agent[Int]): Props = {
    Props(new FillCountingActor(counter))
  }
}