import akka.actor.{PoisonPill, ActorRef, ActorSystem}
import akka.agent.Agent

import java.io.PrintWriter

import Reaper.WatchMe
import actors.{ZILiquidityMarketMaker, RandomTraderConfig}
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


object Application extends App {

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
  import model.dispatcher

  val counter = Agent[Int](0)

  val numberRoutees = config.getInt("settlement.numberRoutees")
  val settlementMechanism = model.actorOf(SettlementRouter.props(counter, numberRoutees))

  // create some tickers
  val tickers = securities.map {
    security => security -> Agent(immutable.Seq.empty[Tick])
  } (collection.breakOut): mutable.Map[Tradable, Agent[immutable.Seq[Tick]]]

  // Create some markets
  val referencePrice = config.getLong("market.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)

  val markets = securities.map { security =>
    val props = MarketActor.props(matchingEngine, settlementMechanism, tickers(security), security)
    security -> model.actorOf(props.withDispatcher("market-dispatcher"))
  } (collection.breakOut): mutable.Map[Tradable, ActorRef]

  // Create some traders
  val numberTraders = config.getInt("traders.number")
  //val traderType = config.getString("traders.type")
  val traderConfig = new RandomTraderConfig(config.getConfig("traders.params"))
  val traders = immutable.IndexedSeq.fill(numberTraders) {
    //model.actorOf(Class.forName(traderType).asInstanceOf[Actor]
    model.actorOf(ZILiquidityMarketMaker.props(traderConfig, markets, prng, tickers))
  }

  // Initialize the reaper
  val reaper = model.actorOf(ProductionReaper.props(counter))
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  model.scheduler.scheduleOnce(1.minute) {
    tickers.foreach {
      case (tradable, ticker) =>
        val jsonTicks = convertTicksToJson(ticker.get)
        writeTicksToFile(jsonTicks, "./data/" + tradable.symbol + ".json")
    }
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
    println(counter.get)
  }
}
