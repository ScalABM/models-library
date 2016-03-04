package strategies.placement

import com.typesafe.config.Config


class RandomOrderPlacementStrategyConfig(config: Config) {

  val arrivalRate: Double = config.getDouble("arrivalRate")

  val timeUnit: String = config.getString("timeUnit")

}
