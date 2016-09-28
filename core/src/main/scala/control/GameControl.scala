package control

import akka.actor.{Actor, ActorLogging}
import model.Level
import msg.ClientMessage

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  val mainControl = context.actorSelection("..")

  override def receive = {
    case ClientMessage.UpdateBlockPosition(index, position) =>

    case msg => log.warning("Unhandled message: " + msg)
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
