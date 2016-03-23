package strategies.placement

import scala.concurrent.duration.FiniteDuration
import scala.util.Random


class RandomOrderPlacementStrategy(val config: RandomOrderPlacementStrategyConfig,
                                   val prng: Random) {

  val arrivalRate: Double = config.arrivalRate

  val timeUnit: String = config.timeUnit

  def waitTime(): FiniteDuration = {
    FiniteDuration((-Math.log(prng.nextDouble()) / arrivalRate).toLong, timeUnit)
  }

}


object RandomOrderPlacementStrategy {

  def apply(config: RandomOrderPlacementStrategyConfig,
            prng: Random): RandomOrderPlacementStrategy = {
    new RandomOrderPlacementStrategy(config, prng)
  }

}
