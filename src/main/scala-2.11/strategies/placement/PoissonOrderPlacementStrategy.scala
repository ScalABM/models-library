package strategies.placement

import akka.actor.Scheduler

import markets.participants.strategies.OrderPlacementStrategy

import scala.concurrent.duration._
import scala.util.Random


case class PoissonOrderPlacementStrategy(prng: Random, scheduler: Scheduler)
  extends OrderPlacementStrategy {

  def waitTime(rate: Double): FiniteDuration = {
    (-Math.log(prng.nextDouble()) / rate).millis  // @todo get rid of implicit conversions!
  }

}
