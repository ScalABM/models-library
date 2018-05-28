package strategies

import markets.strategies.investment.UniformRandomInvestmentStrategy
import markets.orders.Order
import markets.strategies.OrderIssuingStrategy
import org.apache.commons.math3.random.RandomGenerator


class ZIOrderIssuingStrategy[T <: Order](prng: RandomGenerator,
                                         config: ZIOrderIssuingStrategyConfig[T])
  extends OrderIssuingStrategy[T] {

  val investmentStrategy = UniformRandomInvestmentStrategy[T](prng)

  val tradingStrategy = ZITradingStrategy[T](prng, config.tradingStrategyConfig)

}


object ZIOrderIssuingStrategy {

  def apply[T <: Order](prng: RandomGenerator,
                        config: ZIOrderIssuingStrategyConfig[T]): ZIOrderIssuingStrategy[T] = {
    new ZIOrderIssuingStrategy[T](prng, config)
  }

}