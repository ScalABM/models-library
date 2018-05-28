package strategies


case class ZITradingStrategyConfig[T](minimumPrice: Long, maximumPrice: Long, quantity: Long) {

  require(minimumPrice > 0, "Min price must be strictly positive.")
  require(minimumPrice <= maximumPrice, "Min price must be less than (or equal to) max price.")
  require(quantity > 0, "Quantity must be strictly positive.")

}
