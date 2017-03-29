package model.general

import akka.actor.{Actor, ActorLogging}

/**
  * Default Actor, warns about all receives messages.
  */
final class DefaultActor extends Actor with ActorLogging {
  log.debug("Initializing")

  override def receive: Receive = {
    case msg => unhandled(msg)
  }

  override def postStop(): Unit = {
    log.debug("Stopping")
  }

}
