package control

import akka.actor.{Actor, ActorLogging}
import model.Level
import msg.ServerMessage.ShowGame

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  val mainControl = context.actorSelection("..")
  mainControl ! ShowGame(level)

  override def receive = {
    case _ => log.warning("TODO")
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
