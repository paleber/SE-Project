package control

import akka.actor.{Actor, ActorLogging}


class MenuControl extends Actor with ActorLogging {
  log.debug("Initializing")

  val mainControl = context.actorSelection("..")

  override def receive = {
    case msg => log.error("Unknown message: " + msg)
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
