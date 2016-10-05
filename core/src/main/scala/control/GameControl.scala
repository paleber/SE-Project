package control

import akka.actor.{Actor, ActorLogging}
import loader.LevelLoader
import model.{Block, Level, Point, Vector}
import msg.{ClientMessage, ServerMessage}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  private val blocks = level.blocks.toArray

  private val boardAnchors = mutable.Map[Point, Option[Int]]()
  level.board.anchors.foreach(a => boardAnchors.put(a, None))

  private val restAnchors = mutable.Map[Point, Option[Int]]()
  level.freeAnchors.foreach(a => restAnchors.put(a, None))

  private var running = true

  /* TODO auto-anchor at start and tell views
  for(i <- blocks.indices) {
    blocks(i) = blocks(i).copy(position = Point(level.width / 2, level.height / 2 + 1))
    anchorOnRest(i)
    AnchorHelper.blockAnchorsAround(i,
      blocks(i).grid.anchors.toArray.transform(p => p + blocks(i).position).toList,
      LevelLoader.minAnchorDistanceSquare(level.rotationSteps),
      restAnchors)
  } */

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
    AnchorHelper.freeAnchorsWithIndex(index, boardAnchors)
    AnchorHelper.freeAnchorsWithIndex(index, restAnchors)

    for (i <- blocks.indices if i != index) {
      AnchorHelper.blockAnchorsAround(
        i,
        blocks(i).grid.anchors.toArray.transform(p => p + blocks(i).position).toList,
        LevelLoader.minAnchorDistanceSquare(level.rotationSteps),
        restAnchors)
    }

    val anchored = anchorOnBoard(index)
    if (!anchored) {
      anchorOnRest(index)
      AnchorHelper.blockAnchorsAround(index,
        blocks(index).grid.anchors.toArray.transform(p => p + blocks(index).position).toList,
        LevelLoader.minAnchorDistanceSquare(level.rotationSteps),
        restAnchors)
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

  private def anchorOnRest(index: Int): Unit = {
    val restList = AnchorHelper.getFreeAnchors(restAnchors)
    while (restList.nonEmpty) {
      val anchor = AnchorHelper.findNearest(
        blocks(index).grid.anchors.head + blocks(index).position,
        restList)
      assert(anchor.isDefined)
      restList -= anchor.get

      val anchored = AnchorHelper.anchorOnAnchor(anchor.get, index, blocks, restAnchors)
      if (anchored) {
        return
      }
    }
  }

  private def anchorOnBoard(index: Int): Boolean = {
    val point = blocks(index).grid.anchors.head + blocks(index).position
    val boardAnchor = AnchorHelper.findNearest(point, AnchorHelper.getFreeAnchors(boardAnchors), 0.5)
    if (boardAnchor.isEmpty) {
      return false
    }
    AnchorHelper.anchorOnAnchor(boardAnchor.get, index, blocks, boardAnchors)
  }


  override def postStop = {
    log.debug("Stopping")
  }

}


case object AnchorHelper {

  def anchorOnAnchor(anchor: Point, blockIndex: Int, blocks: Array[Block], anchorMap: mutable.Map[Point, Option[Int]]): Boolean = {
    val point = blocks(blockIndex).grid.anchors.head + blocks(blockIndex).position
    val diff = Vector.stretch(point, anchor)
    val blockCopy = blocks(blockIndex).copy(
      position = blocks(blockIndex).position + diff
    )

    for (blockAnchor <- blockCopy.grid.anchors) {
      val boardAnchor = AnchorHelper.findNearest(blockAnchor + blockCopy.position, AnchorHelper.getFreeAnchors(anchorMap), 1e-3)
      if (boardAnchor.isEmpty || anchorMap(boardAnchor.get).isDefined) {
        AnchorHelper.freeAnchorsWithIndex(blockIndex, anchorMap)
        return false
      }
      anchorMap(boardAnchor.get) = Some(blockIndex)

    }
    blocks(blockIndex) = blockCopy
    true
  }


  def freeAnchorsWithIndex(index: Int, anchorMap: mutable.Map[Point, Option[Int]]): Unit = {
    anchorMap.foreach { case (k, v) =>
      if (v.isDefined && index == v.get) {
        println("freeing: " + index)
        anchorMap(k) = None
      }
    }
  }

  def findNearest(point: Point,
                  anchors: ListBuffer[Point],
                  maxDistance: Double = Double.PositiveInfinity): Option[Point] = {
    var minDistanceSquare = maxDistance * maxDistance
    var nearestAnchor: Option[Point] = None
    anchors.foreach(a => {
      val distanceSquare = a.distanceSquareTo(point)
      if (distanceSquare < minDistanceSquare) {
        minDistanceSquare = distanceSquare
        nearestAnchor = Some(a)
      }
    })
    nearestAnchor
  }

  def getFreeAnchors(anchorMap: mutable.Map[Point, Option[Int]]): ListBuffer[Point] = {
    val anchorList = ListBuffer.empty[Point]
    anchorMap.foreach { case (k, v) =>
      if (v.isEmpty) {
        anchorList += k
      }
    }
    anchorList
  }

  def blockAnchorsAround(index: Int,
                         anchors: List[Point],
                         maxDistanceSquare: Double,
                         anchorMap: mutable.Map[Point, Option[Int]]): Unit = {

    anchors.foreach(a => {
      anchorMap.foreach { case (k, v) =>
        if (v.isEmpty && a.distanceSquareTo(k) < maxDistanceSquare) {
          anchorMap(k) = Some(index)
        }
      }
    })

  }

}