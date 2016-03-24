package strategies.trading

import akka.agent.Agent

import com.typesafe.config.ConfigFactory
import markets.tickers.Tick
import markets.tradables.{Security, Tradable}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random


class ZICLimitOrderTradingStrategySpec extends FlatSpec with Matchers {

  val config = ConfigFactory.load("test.conf").getConfig("traders.strategies.trading")
  val strategyConfig = new ZILimitOrderTradingStrategyConfig(config)

  val prng = new Random(42)
  val tradable = Security("GOOG")
  val tickers = mutable.Map[Tradable, Agent[Tick]](tradable -> Agent(Tick(1, 1, 1, 1, 1)))
  val valuations = mutable.Map[Tradable, Long](tradable -> 2)


  "ZICLimitOrderTradingStrategy" should "generate ask prices that are greater than valuations" in {

    val strategy = ZICLimitOrderTradingStrategy(strategyConfig, prng, valuations)
    strategy.askPrice(tickers(tradable), tradable) should be >= valuations(tradable)

  }

  "ZICLimitOrderTradingStrategy" should "generate bid prices that are less than valuations" in {

    val strategy = ZICLimitOrderTradingStrategy(strategyConfig, prng, valuations)
    strategy.bidPrice(tickers(tradable), tradable) should be <= valuations(tradable)

  }

}
