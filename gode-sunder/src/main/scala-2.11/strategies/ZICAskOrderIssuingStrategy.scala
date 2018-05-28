package strategies

import markets.orders.AskOrder
import markets.strategies.OrderIssuingStrategy
import markets.tradables.Tradable
import org.apache.commons.math3.random.RandomGenerator


class ZICAskOrderIssuingStrategy(prng: RandomGenerator,
                                 quantity: Long,
                                 valuations: Map[Tradable, Long])
  extends OrderIssuingStrategy[AskOrder] {

  val investmentStrategy = ZICAskOrderInvestmentStrategy(prng, valuations)

  val tradingStrategy = ZICAskOrderTradingStrategy(prng, quantity, valuations)

}


object ZICAskOrderIssuingStrategy {

  def apply(prng: RandomGenerator,
            quantity: Long,
            valuations: Map[Tradable, Long]): ZICAskOrderIssuingStrategy = {
    new ZICAskOrderIssuingStrategy(prng, quantity, valuations)
  }

  def apply(config: ZICOrderIssuingStrategyConfig,
            prng: RandomGenerator): ZICAskOrderIssuingStrategy = {
    val quantity = config.quantity
    new ZICAskOrderIssuingStrategy(quantity, prng)
  }

}