package util

import akka.actor.{Actor, ActorLogging}

/**
  * Default Actor, warns about all receives messages.
  */
final class DefaultActor extends Actor with ActorLogging {
  log.debug("Initializing")

  override def receive = {
    case msg => log.warning(s"Unhandled message: $msg")
  }

  override def postStop(): Unit = {
    log.debug("Stopping")
  }

}
