package strategies

import akka.agent.Agent

import markets.orders.AskOrder
import markets.strategies.trading.{ConstantQuantity, TradingStrategy}
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.distribution.UniformRealDistribution
import org.apache.commons.math3.random.RandomGenerator


class ZICTradingStrategy(prng: RandomGenerator,
                         config: ZICTradingStrategyConfig[AskOrder])
  extends TradingStrategy[AskOrder]
  with ConstantQuantity[AskOrder]
  with TradableValuations[AskOrder] {

  val quantity = config.quantity

  val valuations = config.valuations

  def apply(tradable: Tradable, ticker: Agent[Tick]): Option[(Option[Long], Long)] = {
    val valuation = valuations(tradable)
    val marketPrice = ticker.get.price

    if (valuation < marketPrice ){
      val priceDistribution = getDistribution(prng, valuation, marketPrice)
      Some(Some(Math.round(priceDistribution.sample())), quantity)
    } else {
      None
    }
  }

  private[this] def getDistribution(rng: RandomGenerator, lower: Double, upper: Double) = {
    new UniformRealDistribution(rng, lower, upper)
  }

}
