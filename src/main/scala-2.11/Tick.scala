/** Class respresenting a Tick.
  *
  * @param askPrice
  * @param bidPrice
  * @param price
  * @param quantity
  */
case class Tick(askPrice: Long,
                bidPrice: Long,
                price: Long,
                quantity: Long)
