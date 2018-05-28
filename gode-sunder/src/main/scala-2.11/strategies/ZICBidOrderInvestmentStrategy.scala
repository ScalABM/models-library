package strategies

import akka.agent.Agent

import markets.orders.BidOrder
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.random.RandomGenerator


class ZICBidOrderInvestmentStrategy(prng: RandomGenerator,
                                    val valuations: Map[Tradable, Long])
  extends UniformRandomInvestmentStrategy[BidOrder](prng)
    with TradableValuations[BidOrder] {

  override def apply(information: Map[Tradable, Agent[Tick]]): Option[Tradable] = {
    val feasibleTradables = overValuedTradables(information)
    if (feasibleTradables.isEmpty) None else Some(generateSamplingDistribution(feasibleTradables.keySet).sample())
  }

}
