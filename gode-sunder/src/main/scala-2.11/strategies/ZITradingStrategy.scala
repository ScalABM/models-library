/*
Copyright 2016 David R. Pugh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package strategies

import akka.agent.Agent

import markets.orders.Order
import markets.strategies.trading.{ConstantQuantity, RandomPrice, TradingStrategy}
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.distribution.UniformRealDistribution
import org.apache.commons.math3.random.RandomGenerator


/** Stub implementation of the `RandomTradingStrategy` trait for testing purposes.
  *
  * @param prng
  * @param config
  * @tparam T
  */
class ZITradingStrategy[T <: Order](prng: RandomGenerator, config: ZITradingStrategyConfig[T])
  extends TradingStrategy[T]
  with RandomPrice[T]
  with ConstantQuantity[T] {

  val priceDistribution = {
    new UniformRealDistribution(prng, config.minimumPrice, config.maximumPrice)
  }

  val quantity = config.quantity

  def apply(tradable: Tradable, ticker: Agent[Tick]): Option[(Option[Long], Long)] = {
    Some(specifyPrice(), quantity)
  }

  private[this] def specifyPrice(): Option[Long] = {
    Some(Math.round(priceDistribution.sample()))
  }


}


object ZITradingStrategy {

  def apply[T <: Order](prng: RandomGenerator,
                        config: ZITradingStrategyConfig[T]): ZITradingStrategy[T] = {
    new ZITradingStrategy[T](prng, config)
  }

}