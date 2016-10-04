package control

import akka.actor.{Actor, ActorLogging}
import model.{Level, Point, Vector}
import msg.{ClientMessage, ServerMessage}

import scala.collection.mutable

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  private val blocks = level.blocks.toArray

  private val boardAnchors = mutable.Map[Point, Option[Int]]()
  level.board.anchors.foreach(a => boardAnchors.put(a, None))

  private val freeAnchors = mutable.Map[Point, Option[Int]]()
  level.freeAnchors.foreach(a => freeAnchors.put(a, None))



  private var running = true

  private def doBlockAction(index: Int)(function: => Unit): Unit = {
    if (blocks.lift(index).isEmpty) {
      log.error("Invalid block index: " + index)
    } else if (!running) {
      log.warning("Action while level is finished")
    } else {
      function
      anchorBlock(index)
    }
  }

  override def receive = {

    case ClientMessage.UpdateBlockPosition(index, position) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          position = position
        )
      }

    case ClientMessage.RotateBlockLeft(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          grid = blocks(index).grid.rotate(-Math.PI * 2 / level.rotationSteps)
        )
      }

    case ClientMessage.RotateBlockRight(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          grid = blocks(index).grid.rotate(Math.PI * 2 / level.rotationSteps)
        )
      }

    case ClientMessage.MirrorBlockVertical(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          grid = blocks(index).grid.mirrorVertical()
        )
      }

    case ClientMessage.MirrorBlockHorizontal(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          grid = blocks(index).grid.mirrorHorizontal()
        )
      }

    case msg => log.warning("Unhandled message: " + msg)
  }




  private def anchorBlock(index: Int): Unit = {
    freeAnchorsWithBlock(index)

    val anchored = anchorOnBoard(index)
    if (!anchored) {
      blocks(index) = blocks(index).copy(
        position = level.blocks(index).position
      )
    }

    context.parent ! ServerMessage.UpdateBlock(index, blocks(index))
    if (anchored) {
      checkLevelFinished()
    }
  }

  private def checkLevelFinished(): Unit = {
    boardAnchors.values.foreach(f =>
      if (f.isEmpty) {
        return
      }
    )
    running = false
    context.parent ! ServerMessage.LevelFinished
  }

  private def anchorOnFree(index: Int): Unit = {

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
      val boardAnchor = findNextBoardAnchor(blockAnchor + blockCopy.position, 0.1)
      if (boardAnchor.isEmpty || boardAnchors(boardAnchor.get).isDefined) {
        freeAnchorsWithBlock(index)
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

  private def freeAnchorsWithBlock(blockIndex: Int): Unit = {
    boardAnchors.foreach { case (anchor, index) =>
      if (index.isDefined && blockIndex == index.get) {
        boardAnchors(anchor) = None
      }
    }
    freeAnchors.foreach { case (anchor, index) =>
      if (index.isDefined && blockIndex == index.get) {
        freeAnchors(anchor) = None
      }
    }
  }

  override def postStop = {
    log.debug("Stopping")
  }

}
