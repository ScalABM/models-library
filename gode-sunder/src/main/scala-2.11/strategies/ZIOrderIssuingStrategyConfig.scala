package strategies

import markets.orders.Order


case class ZIOrderIssuingStrategyConfig[T <: Order](investmentStrategyConfig: ZIInvestmentStrategyConfig[T],
                                                    tradingStrategyConfig: ZITradingStrategyConfig[T])

object ZIOrderIssuingStrategyConfig {

  def apply[T <: Order](investmentStrategyConfig: ZIInvestmentStrategyConfig[T],
                        tradingStrategyConfig: ZITradingStrategyConfig[T]): ZIOrderIssuingStrategyConfig = {
    new ZIOrderIssuingStrategyConfig(investmentStrategyConfig, tradingStrategyConfig)
  }

}
