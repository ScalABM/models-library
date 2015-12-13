import akka.actor.{PoisonPill, Kill, ActorRef, Props, ActorSystem}
import akka.agent.Agent

import com.typesafe.config.ConfigFactory
import markets.MarketActor
import markets.clearing.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tradables.{Security, Tradable}

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random


object Application extends App {

  val config = ConfigFactory.load()

  // set the seed
  val prng = new Random(42)

  // Create some tradable securities...
  val numberTradables = config.getInt("tradables.number")
  val securities = immutable.Seq.fill[Tradable](numberTradables){
    Security(prng.alphanumeric.take(4).mkString)
  }

  // Create some tickers...
  val model = ActorSystem("model", config)
  val settlementMechanism = model.actorOf(Props[LoggingSettlementMechanismActor])

  // Create some markets
  val referencePrice = config.getLong("market.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)
  val markets = securities.map {
    s => s -> (model.actorOf(MarketActor.props(matchingEngine, settlementMechanism, s)
      .withDispatcher("market-dispatcher")), Agent(Tick(1, 1, Some(1), 1)))
  } (collection.breakOut): immutable.Map[Tradable, (ActorRef, Agent[Tick])]

  // Create some simple traders
  val numberTraders = config.getInt("traders.number")
  val traders = immutable.IndexedSeq.fill(numberTraders) {
    val askOrderProbability = config.getDouble("traders.askOrderProbability")
    model.actorOf(RandomTradingActor.props(askOrderProbability, prng, markets))
  }

  model.scheduler.scheduleOnce(1.minute) {
    //traders.foreach(trader => trader ! PoisonPill)
    model.terminate()
  }
}
