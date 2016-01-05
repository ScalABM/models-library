package strategies.placement

import akka.actor.Scheduler

import scala.concurrent.duration._
import scala.util.Random


case class PoissonOrderPlacementStrategy(prng: Random, rate: Double, scheduler: Scheduler)
  extends RandomOrderPlacementStrategy {

  def waitTime(): FiniteDuration = {
    (-Math.log(prng.nextDouble()) / rate).millis  // @todo get rid of implicit conversions!
  }

}
