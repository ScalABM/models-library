package strategies

import markets.orders.Order
import markets.tradables.Tradable


class ZICTradingStrategyConfig[T <: Order](minimumPrice: Long,
                                           maximumPrice: Long,
                                           quantity: Long,
                                           val valuations: Map[Tradable, Long])
  extends ZITradingStrategyConfig[T](minimumPrice, maximumPrice, quantity) {

}
