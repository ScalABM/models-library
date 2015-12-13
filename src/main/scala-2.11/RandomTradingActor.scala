import akka.actor.{Props, ActorRef}
import akka.agent.Agent

import markets.Cancel
import markets.BaseActor
import markets.orders.Order
import markets.orders.limit.{LimitOrderLike, LimitBidOrder, LimitAskOrder}
import markets.orders.market.{MarketOrderLike, MarketAskOrder, MarketBidOrder}
import markets.participants.MarketParticipantLike
import markets.tradables.Tradable

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random


case class RandomTradingActor(askOrderProbability: Double,
                              prng: Random,
                              var markets: immutable.Map[Tradable, (ActorRef, Agent[Tick])])

  extends BaseActor
  with MarketParticipantLike {

  protected var outstandingOrders = immutable.Set.empty[Order]

  // arrival rate of limit orders
  val alpha = 0.1

  // arrival rate of market orders...
  val mu = 0.2

  // arrival rate of order cancellations...
  val delta = 0.15

  // notify the trader to start sending limit orders...
  context.system.scheduler.scheduleOnce(waitTime(prng, alpha)) {
    self ! GenerateLimitOrder
  }

  // notify the trader to start sending market orders...
  context.system.scheduler.scheduleOnce(waitTime(prng, mu)) {
    self ! GenerateMarketOrder
  }

  // notify the trader to start sending order cancellations...
  context.system.scheduler.scheduleOnce(waitTime(prng, delta)) {
    self ! CancelOrder
  }

  //context.system.scheduler.schedule(10000.millis, 10000.millis) {
  //  println(context.self.path.name + ":" + outstandingOrders.size)
  //}

  /** Random price between 1 and upper */
  def price(prng: Random, lower: Long = 1, upper: Long = Long.MaxValue): Long = {
    Math.exp(Math.log(lower) + (Math.log(upper) - Math.log(lower)) * prng.nextDouble()).toLong
  }

  def quantity(prng: Random, lower: Long = 1, upper: Long = Long.MaxValue): Long = {
    Math.exp(Math.log(lower) + (Math.log(upper) - Math.log(lower)) * prng.nextDouble()).toLong
  }

  def waitTime(prng: Random, rate: Double): FiniteDuration = {
    (-Math.log(prng.nextDouble()) / rate).millis
  }

  def investmentStrategy(): (Tradable, (ActorRef, Agent[Tick])) = {
    prng.shuffle(markets).head
  }

  def orderCancellationStrategy(): Option[Order] = {
    prng.shuffle(outstandingOrders).headOption
  }

  def generateLimitOrder(tradable: Tradable, ticker: Agent[Tick]): LimitOrderLike = {
    val tick = ticker.get()
    if (prng.nextDouble() < askOrderProbability) {
      val askPrice = price(prng, lower = tick.bidPrice)
      LimitAskOrder(self, askPrice, quantity(prng), timestamp(), tradable, uuid())
    } else {
      val bidPrice = price(prng, upper = tick.askPrice)
      LimitBidOrder(self, bidPrice, quantity(prng), timestamp(), tradable, uuid())
    }

  }

  def generateMarketOrder(tradable: Tradable): MarketOrderLike = {
    if (prng.nextDouble() < askOrderProbability) {
      MarketAskOrder(self, quantity(prng), timestamp(), tradable, uuid())
    } else {
      MarketBidOrder(self, quantity(prng), timestamp(), tradable, uuid())
    }

  }

  def tradingBehavior: Receive = {
    case GenerateLimitOrder =>
      val (tradable, (market, ticker)) = investmentStrategy()
      market ! generateLimitOrder(tradable, ticker)
      context.system.scheduler.scheduleOnce(waitTime(prng, alpha)) {
        self ! GenerateLimitOrder
      }
    case GenerateMarketOrder =>
      val (tradable, (market, _)) = investmentStrategy()
      market ! generateMarketOrder(tradable)
      context.system.scheduler.scheduleOnce(waitTime(prng, mu)) {
        self ! GenerateMarketOrder
      }
    case CancelOrder =>
      val order = orderCancellationStrategy()
      order match {
        case Some(outstandingOrder) =>
          val (market, _) = markets(outstandingOrder.tradable)
          market ! Cancel(outstandingOrder, timestamp(), uuid())
        case None =>  // no outstanding order to cancel!
      }
      context.system.scheduler.scheduleOnce(waitTime(prng, delta)) {
        self ! CancelOrder
      }

  }

  def receive: Receive = {
    tradingBehavior orElse marketParticipantBehavior orElse baseActorBehavior
  }

  private object CancelOrder

  private object GenerateMarketOrder

  private object GenerateLimitOrder

}


object RandomTradingActor {

  def props(askOrderProbability: Double,
            prng: Random,
            markets: immutable.Map[Tradable, (ActorRef, Agent[Tick])]): Props = {
    Props(new RandomTradingActor(askOrderProbability, prng, markets))
  }
}