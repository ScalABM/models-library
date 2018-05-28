package actors

import markets.Fill
import markets.actors.StackableActor


class FillCountingActor extends StackableActor {

  wrappedBecome(fillCountingBehavior)

  def fillCountingBehavior: Receive = {
    case fill: Fill =>
      count += 1
  }

  private[this] var count = 0

}
