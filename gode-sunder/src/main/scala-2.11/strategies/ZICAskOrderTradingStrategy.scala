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

import markets.orders.AskOrder
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.distribution.UniformRealDistribution
import org.apache.commons.math3.random.RandomGenerator


/** Class implementing the Zero Intelligence (ZI) trading strategy from Gode-Sunder (JPE, 1996).
  *
  * @param quantity the specific quantity to use for all orders.
  * @param prng some[[org.apache.commons.math3.random.RandomGenerator `RandomGenerator`]] instance.
  */
case class ZICAskOrderTradingStrategy(prng: RandomGenerator,
                                      quantity: Long,
                                      valuations: Map[Tradable, Long])
  extends ZICTradingStrategy[AskOrder] {

  def generatePriceDistribution(tradable: Tradable, ticker: Agent[Tick]) = {
    val valuation = valuations(tradable)
    val marketPrice = ticker.get.price
    if (valuation <= marketPrice) {
      Some(new UniformRealDistribution(prng, valuation, marketPrice))
    } else {
      None
    }
  }

}

