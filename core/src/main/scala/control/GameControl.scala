package control

import akka.actor.{Actor, ActorLogging}
import model.Level
import msg.{ClientMessage, ServerMessage}

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  val mainControl = context.actorSelection("..")

  val blocks = level.blocks.toArray


  override def receive = {
    case ClientMessage.UpdateBlockPosition(index, position) =>
      mainControl ! ServerMessage.UpdateBlock(index, blocks(index))

    case msg => log.warning("Unhandled message: " + msg)
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
