package actors

import akka.actor.{ActorRef, Props}
import akka.agent.Agent

import markets.actors.participants.issuers.OrderIssuer
import markets.strategies.{GodeSunderZIOrderIssuingStrategy, GodeSunderZIOrderIssuingStrategyConfig}
import markets.orders.{AskOrder, BidOrder}
import markets.tickers.Tick
import markets.tradables.Tradable
import org.apache.commons.math3.random.MersenneTwister
import strategies.{ZIOrderIssuingStrategy, ZIOrderIssuingStrategyConfig}


class ZIOrderIssuer(val config: ZIOrderIssuerConfig) extends OrderIssuer {

  val prng = {
    config.seed match {
      case Some(long) =>
        new MersenneTwister(long)
      case None =>
        new MersenneTwister()
    }
  }

  val askOrderIssuingStrategyConfig = {
    ZIOrderIssuingStrategyConfig(config.askOrderIssuingStrategyConfig)
  }

  val askOrderIssuingStrategy = {
    ZIOrderIssuingStrategy[AskOrder](askOrderIssuingStrategyConfig, prng)
  }

  val bidOrderIssuingStrategyConfig = {
    ZIOrderIssuingStrategyConfig(config.bidOrderIssuingStrategyConfig)
  }

  val bidOrderIssuingStrategy = {
    ZIOrderIssuingStrategy[BidOrder](bidOrderIssuingStrategyConfig, prng)
  }

  var tickers = Map.empty[Tradable, Agent[Tick]]

  var markets = Map.empty[Tradable, ActorRef]

}


object ZIOrderIssuer {

  def props(config: ZIOrderIssuerConfig): Props = {
    Props(new ZIOrderIssuer(config))
  }

}