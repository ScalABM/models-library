import akka.actor.{PoisonPill, ActorRef, Props, ActorSystem}
import akka.agent.Agent

import java.io.PrintWriter

import actors.{SimpleSettlementMechanismActor, ZILiquidityDemander, PassiveLiquiditySupplier, RandomTraderConfig}
import com.typesafe.config.ConfigFactory
import markets.MarketActor
import markets.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tickers.Tick
import markets.tradables.{Security, Tradable}
import play.api.libs.json.{Json, JsValue}

import scala.collection.{mutable, immutable}
import scala.concurrent.duration._
import scala.util.Random


object FarmerEtAlApp extends BaseApp with App {

  val config = ConfigFactory.load("farmerEtAlModel.conf")

  val model = ActorSystem("farmer-et-al-model", config)

  val path: String = "./data/farmer-et-al-model/"

  val prng = new Random(42)

  // Create some tradable Securities...
  val numberMarkets = config.getInt("markets.number")
  val tradables = immutable.Seq.fill[Tradable](numberMarkets){
    Security(prng.alphanumeric.take(4).mkString)
  }

  // Create a simple settlement mechanism
  val settlementProps = Props[SimpleSettlementMechanismActor]
  val settlementMechanism = model.actorOf(settlementProps, "settlement-mechanism")

  // Create a collection of tickers (one for each tradable security)
  val tickers = tradables.map {
    security => security -> Agent(immutable.Seq.empty[Tick])(model.dispatcher)
  } (collection.breakOut): mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]

  // Create a collection of markets (one for each tradable security)
  val referencePrice = config.getLong("markets.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)
  val markets = tradables.map { security =>
    val props = MarketActor.props(matchingEngine, settlementMechanism, tickers(security), security)
    security -> model.actorOf(props.withDispatcher("markets.dispatcher"))
  } (collection.breakOut): mutable.Map[Tradable, ActorRef]

  // Create some traders
  val numberAggressive = config.getInt("traders.numberAggressive")
  val numberPassive = config.getInt("traders.numberPassive")
  val traderConfig = new RandomTraderConfig(config.getConfig("traders.params"))
  val aggressiveTraders = immutable.IndexedSeq.fill(numberAggressive) {
    val props = ZILiquidityDemander.props(traderConfig, markets, prng, tickers)
    model.actorOf(props.withDispatcher("traders.dispatcher"))
  }
  val passiveTraders = immutable.IndexedSeq.fill(numberPassive) {
    val props = PassiveLiquiditySupplier.props(traderConfig, markets, prng, tickers)
    model.actorOf(props.withDispatcher("traders.dispatcher"))
  }
  val traders = aggressiveTraders ++ passiveTraders

  // Initialize the Reaper
  val reaper = model.actorOf(Props[Reaper])
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  model.scheduler.scheduleOnce(1.minute) {
    tickers.foreach {
      case (tradable, ticker) =>
        val jsonTicks = convertTicksToJson(ticker.get)
        writeTicksToFile(jsonTicks, path + tradable.symbol + ".json")
    }
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
  }(model.dispatcher)

}
