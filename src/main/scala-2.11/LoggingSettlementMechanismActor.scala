import markets.settlement.{TrivialSettlementBehavior, SettlementMechanismActor}

class LoggingSettlementMechanismActor extends SettlementMechanismActor
  with TrivialSettlementBehavior {

  var count = 0

  def loggingBehavior: Receive = new Receive {
    def isDefinedAt(x: Any) = {
      count += 1
      println(count)
      false
    }

    def apply(x: Any) = throw new UnsupportedOperationException
  }

  def receive: Receive = {
    loggingBehavior orElse settlementBehavior orElse baseActorBehavior
  }

}
