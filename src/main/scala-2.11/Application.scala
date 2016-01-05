import akka.actor.{PoisonPill, ActorRef, ActorSystem}
import akka.agent.Agent

import Reaper.WatchMe
import actors.{ZILiquidityMarketMaker, RandomTraderConfig}
import com.typesafe.config.ConfigFactory
import markets.MarketActor
import markets.engines.CDAMatchingEngine
import markets.orders.orderings.ask.AskPriceTimeOrdering
import markets.orders.orderings.bid.BidPriceTimeOrdering
import markets.tickers.Tick
import markets.tradables.{Security, Tradable}
import strategies.placement.PoissonOrderPlacementStrategy

import scala.collection.{mutable, immutable}
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
  val counter = Agent[Int](0)

  val numberRoutees = config.getInt("settlement.numberRoutees")
  val settlementMechanism = model.actorOf(SettlementRouter.props(counter, numberRoutees))

  // create some tickers
  val tickers = securities.map {
    security => security -> Agent(Tick(1, 1, Some(1), 1, 1))
  } (collection.breakOut): mutable.Map[Tradable, Agent[Tick]]

  // Create some markets
  val referencePrice = config.getLong("market.referencePrice")
  val matchingEngine = CDAMatchingEngine(AskPriceTimeOrdering, BidPriceTimeOrdering, referencePrice)

  val markets = securities.map { security =>
    val props = MarketActor.props(matchingEngine, settlementMechanism, tickers(security), security)
    security -> model.actorOf(props.withDispatcher("market-dispatcher"))
  } (collection.breakOut): mutable.Map[Tradable, ActorRef]

  // Create some traders
  val numberTraders = config.getInt("traders.number")
  val traderConfig = new RandomTraderConfig(config.getConfig("traders.params"))
  val orderPlacementStrategy = new PoissonOrderPlacementStrategy(prng, 2.0, model.scheduler)
  val traders = immutable.IndexedSeq.fill(numberTraders) {
    model.actorOf(ZILiquidityMarketMaker.props(traderConfig, markets, orderPlacementStrategy, prng, tickers))
  }

  // Initialize the reaper
  val reaper = model.actorOf(ProductionReaper.props(counter))
  markets.foreach {
    case (tradable: Tradable, market: ActorRef) => reaper ! WatchMe(market)
  }
  traders.foreach(trader => reaper ! WatchMe(trader))
  reaper ! WatchMe(settlementMechanism)

  model.scheduler.scheduleOnce(1.minute) {
    traders.foreach(trader => trader ! PoisonPill)
    markets.foreach {
      case (tradable: Tradable, market: ActorRef) => market ! PoisonPill
    }
    settlementMechanism ! PoisonPill
    println(counter.get)
  }
}
