package strategies

import akka.agent.Agent

import markets.orders.AskOrder
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.random.RandomGenerator


class ZICAskOrderInvestmentStrategy(prng: RandomGenerator,
                                    val valuations: Map[Tradable, Long])
  extends UniformRandomInvestmentStrategy[AskOrder](prng)
    with TradableValuations[AskOrder] {

  override def apply(information: Map[Tradable, Agent[Tick]]): Option[Tradable] = {
    val feasibleTradables = getUnderValuedTradables(information)
    val samplingDistribution = generateSamplingDistribution(feasibleTradables.keySet)
    if (feasibleTradables.isEmpty) None else Some(samplingDistribution.sample())
  }

}


object ZICAskOrderInvestmentStrategy {

  def apply(valuations: Map[Tradable, Long],
            prng: RandomGenerator): ZICAskOrderInvestmentStrategy = {
    new ZICAskOrderInvestmentStrategy(prng, valuations)
  }

}