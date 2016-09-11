package gui

import akka.actor.{Actor, ActorLogging}
import msg.ClientMessage.RegisterView


class Gui extends Actor with ActorLogging {
  log.debug("Initializing")

  val mainControl = context.actorSelection("../control")
  mainControl ! RegisterView(self)

  override def receive = {
    case msg => log.warning("TODO: " + msg)
  }

}
