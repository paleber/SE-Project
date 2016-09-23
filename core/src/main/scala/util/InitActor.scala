package util

import akka.actor.{Actor, ActorLogging}

/**
  * Actor for initializing phases.
  */
class InitActor extends Actor with ActorLogging {

  override def receive = {
    case msg => log.warning(s"Unhandled message: $msg")
  }

}
