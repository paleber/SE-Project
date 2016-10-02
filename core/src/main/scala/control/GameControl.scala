package control

import akka.actor.{Actor, ActorLogging}
import model.Level
import msg.{ClientMessage, ServerMessage}

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  val blocks = level.blocks.toArray

  override def receive = {
    case ClientMessage.UpdateBlockPosition(index, position) =>
      // TODO move block
      anchorBlock(index)

    case ClientMessage.RotateBlockLeft(index) =>
      val block = blocks.lift(index)
      if (block.isDefined) {
        blocks(index) = block.get.copy(
          grid = block.get.grid.rotate(-Math.PI / 2)
        )
        anchorBlock(index)
      } else {
        log.error("Invalid block index while rotating left: " + index)
      }

    case ClientMessage.RotateBlockRight(index) =>
      val block = blocks.lift(index)
      if (block.isDefined) {
        blocks(index) = block.get.copy(
          grid = block.get.grid.rotate(Math.PI / 2)
        )
        anchorBlock(index)
      } else {
        log.error("Invalid block index while rotating right: " + index)
      }

    case ClientMessage.MirrorBlockVertical(index) =>
      val block = blocks.lift(index)
      if (block.isDefined) {
        blocks(index) = block.get.copy(
          grid = block.get.grid.mirrorVertical()
        )
        anchorBlock(index)
      } else {
        log.error("Invalid block index while rotating right: " + index)
      }

    case ClientMessage.MirrorBlockHorizontal(index) =>
      val block = blocks.lift(index)
      if (block.isDefined) {
        blocks(index) = block.get.copy(
          grid = block.get.grid.mirrorHorizontal()
        )
        anchorBlock(index)
      } else {
        log.error("Invalid block index while rotating right: " + index)
      }

    case msg => log.warning("Unhandled message: " + msg)
  }

  private def anchorBlock(index: Int): Unit = {
    val block = blocks.lift(index)
    if (block.isEmpty) {
      log.error("Invalid block index while anchoring block: " + index)
      return
    }
    // TODO
    context.parent ! ServerMessage.UpdateBlock(index, block.get)
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
