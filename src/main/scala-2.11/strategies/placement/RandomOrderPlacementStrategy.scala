package strategies.placement

import akka.actor.ActorRef

import markets.participants.strategies.OrderPlacementStrategy

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.Random


trait RandomOrderPlacementStrategy extends OrderPlacementStrategy {

  def prng: Random

  def waitTime(): FiniteDuration

  def scheduleOnce(receiver: ActorRef,
                   message: Any)
                  (implicit executionContext: ExecutionContext): Unit = {
    super.scheduleOnce(waitTime(), receiver, message)(executionContext)
  }

  def schedule(receiver: ActorRef,
               message: Any)
              (implicit executionContext: ExecutionContext): Unit = {
    super.schedule(waitTime(), waitTime(), receiver, message)(executionContext)
  }
}
