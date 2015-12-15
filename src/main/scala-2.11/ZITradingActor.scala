import akka.actor.{Props, ActorRef}
import akka.agent.Agent

import markets.orders.Order
import markets.tickers.Tick
import markets.tradables.Tradable

import scala.collection.immutable
import scala.concurrent.duration._
import scala.util.Random


/** Zero intelligence trader emits market and limit orders at a certain rate. */
class ZITradingActor(var markets: immutable.Map[Tradable, (ActorRef, Agent[Tick])],
                     val config: RandomTraderConfig,
                     val prng: Random)
  extends RandomTradingActor
  with ZILiquidityMarketMaker {

  protected var outstandingOrders = immutable.Set.empty[Order]

  scheduleLimitOrder(waitTime(prng, config.alpha), waitTime(prng, config.alpha))

  scheduleMarketOrder(waitTime(prng, config.mu), waitTime(prng, config.mu))

  scheduleOrderCancellation(waitTime(prng, config.delta), waitTime(prng, config.delta))

  def waitTime(prng: Random, rate: Double): FiniteDuration = {
    (-Math.log(prng.nextDouble()) / rate).millis  // @todo get rid of implicit conversions!
  }

  def receive: Receive = {
    marketParticipantBehavior orElse baseActorBehavior
  }

}


object ZITradingActor {

  def props(markets: immutable.Map[Tradable, (ActorRef, Agent[Tick])],
            config: RandomTraderConfig,
            prng: Random): Props = {
    Props(new ZITradingActor(markets, config, prng))
  }

}
