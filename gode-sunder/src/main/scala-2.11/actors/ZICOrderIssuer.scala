package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.actors.participants.issuers.OrderIssuer
import markets.orders.{AskOrder, BidOrder}
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.random.MersenneTwister
import strategies.ZICAskOrderIssuingStrategy


class ZICOrderIssuer(val config: ZICOrderIssuerConfig) extends OrderIssuer {

  val prng = {
    config.seed match {
      case Some(long) =>
        new MersenneTwister(long)
      case None =>
        new MersenneTwister()
    }
  }

  val askOrderIssuingStrategyConfig = {
    ZICOrderIssuingStrategyConfig(config.askOrderIssuingStrategyConfig)
  }

  val askOrderIssuingStrategy = {
    ZICAskOrderIssuingStrategy(askOrderIssuingStrategyConfig, prng)
  }

  val bidOrderIssuingStrategyConfig = {
    ZICBidOrderIssuingStrategyConfig(config.bidOrderIssuingStrategyConfig)
  }


  val bidOrderIssuingStrategy = {
    ZICBidOrderIssuingStrategy(bidOrderIssuingStrategyConfig, prng)
  }

  var tickers = Map.empty[Tradable, Agent[Tick]]

  var markets = Map.empty[Tradable, ActorRef]

}


object ZICOrderIssuer {

  def props(config: ZICOrderIssuerConfig): Props = {
    Props(new ZICOrderIssuer(config))
  }

}