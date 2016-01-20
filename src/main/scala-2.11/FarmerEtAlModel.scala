import akka.actor.{PoisonPill, ActorRef, ActorSystem}
import akka.agent.Agent

import java.io.PrintWriter

import Reaper.WatchMe
import actors.{ZILiquidityDemander, PassiveLiquiditySupplier, RandomTraderConfig}
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


object FarmerEtAlModel extends App {

  def convertTicksToJson(ticks: immutable.Seq[Tick]): JsValue = {
    Json.toJson(
      ticks.map { tick => immutable.Map("askPrice" -> tick.askPrice, "bidPrice" -> tick.bidPrice,
        "price" -> tick.price, "quantity" -> tick.quantity, "timestamp" -> tick.timestamp)
      })
  }

  def writeTicksToFile(json: JsValue, path: String): Unit = {
    val target = new PrintWriter(path)
    target.write(json.toString())
    target.close()
  }

  val config = ConfigFactory.load("farmerEtAlModel.conf")

  // set the seed
  val prng = new Random(42)

  // Create some tradable tradables...
  val numberTradables = config.getInt("markets.number")
  val securities = immutable.Seq.fill[Tradable](numberTradables){
    Security(prng.alphanumeric.take(4).mkString)
  }

  // Create some tickers...
  val model = ActorSystem("model", config)
  import model.dispatcher

  val counter = Agent[Int](0)

  val numberRoutees = config.getInt("markets.settlement.numberRoutees")
  val settlementMechanism = model.actorOf(SettlementRouter.props(counter, numberRoutees))

  // create some tickers
  val tickers = securities.map {
    security => security -> Agent(immutable.Seq.empty[Tick])
  } (collection.breakOut): mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]

  // Create some markets
  val referencePrice = config.getLong("markets.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)

  val markets = securities.map { security =>
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

  // Initialize the reaper
  val reaper = model.actorOf(ProductionReaper.props(counter))
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  model.scheduler.scheduleOnce(1.minutes) {
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
    println(counter.get)
    tickers.foreach {
      case (tradable, ticker) =>
        val jsonTicks = convertTicksToJson(ticker.get)
        writeTicksToFile(jsonTicks, "./data/farmer-et-al-model/" + tradable.symbol + ".json")
    }
  }
}
