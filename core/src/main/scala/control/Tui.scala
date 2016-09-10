package control

import java.lang.IllegalStateException
import java.nio.BufferOverflowException
import java.util.{NoSuchElementException, Scanner}

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, Props}
import akka.camel.Consumer
import msg.ClientMessage.RegisterView
import msg.{ClientMessage, ServerMessage}


class Tui extends Actor with Consumer with ActorLogging {
  log.debug("Initialize")



  override def endpointUri = "stream:in"

  val mainControl = context.actorSelection("../control")
  mainControl ! RegisterView(self)

  override def receive = {
    case msg: String => log.info("Readed: "  + msg)
    case ServerMessage.ShowGame(level) => log.warning("TODO: " + level)
    case msg => log.warning("TODO: " + msg)
  }

}

