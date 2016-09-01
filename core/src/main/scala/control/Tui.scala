package control

import akka.actor.{Actor, ActorLogging, ActorRef}
import msg.ClientMessage.RegisterView


class Tui extends Actor with ActorLogging {
  log.debug("Initialize")

  val mainControl = context.actorSelection("../control")
  mainControl ! RegisterView(self)

  override def receive = {
    case msg => log.warning("TODO: " + msg)
  }

}
