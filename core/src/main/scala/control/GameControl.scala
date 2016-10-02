package control

import akka.actor.{Actor, ActorLogging}
import model.{Level, Point, Vector}
import msg.{ClientMessage, ServerMessage}

import scala.collection.mutable

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  private val blocks = level.blocks.toArray

  private val boardAnchors = mutable.Map[Point, Option[Int]]()
  level.board.anchors.foreach(anchor => boardAnchors.put(anchor, None))

  override def receive = {
    case ClientMessage.UpdateBlockPosition(index, position) =>
      val block = blocks.lift(index)
      if (block.isDefined) {
        blocks(index) = block.get.copy(
          position = position
        )
        anchorBlock(index)
      } else {
        log.error("Invalid block index while updating position: " + index)
      }


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
    freeBoardAnchors(index)

    val anchored = anchorOnBoard(index)

    if (!anchored) {
      blocks(index) = blocks(index).copy(
        position = level.blocks(index).position
      )
    }

    context.parent ! ServerMessage.UpdateBlock(index, blocks(index))
  }

  private def anchorOnBoard(index: Int): Boolean = {

    val point = blocks(index).grid.anchors.head + blocks(index).position
    val boardAnchor = findNextBoardAnchor(point, 0.5)

    if (boardAnchor.isEmpty) {
      return false
    }
    val diff = Vector.stretch(point, boardAnchor.get)
    val blockCopy = blocks(index).copy(
      position = blocks(index).position + diff
    )

    for (blockAnchor <- blockCopy.grid.anchors) {
      val boardAnchor = findNextBoardAnchor(blockAnchor + blockCopy.position, 1)
      if (boardAnchor.isEmpty || boardAnchors(boardAnchor.get).isDefined) {
        freeBoardAnchors(index)
        return false
      }
      boardAnchors(boardAnchor.get) = Some(index)

    }
    blocks(index) = blockCopy
    true
  }

  private def findNextBoardAnchor(point: Point, maxDistance: Double): Option[Point] = {
    var distance = maxDistance
    var nextAnchor: Option[Point] = None
    for (anchor <- level.board.anchors) {
      if (anchor.distanceTo(point) < distance) {
        distance = anchor.distanceTo(point)
        nextAnchor = Some(anchor)
      }
    }
    nextAnchor
  }

  private def freeBoardAnchors(blockIndex: Int): Unit = {
    boardAnchors.foreach { case (anchor, index) =>
      if (index.isDefined && blockIndex == index.get) {
        boardAnchors(anchor) = None
      }
    }
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
