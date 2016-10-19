package control

import akka.actor.{Actor, ActorLogging}
import model.basic.{Point, Vector}
import model.builder.GridBuilder
import model.element.{Block, BlockExtended, Game, Level}
import model.msg.{ClientMsg, InternalMsg, ServerMsg}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class GameControl(level: Level) extends Actor with ActorLogging {
  log.debug("Initializing")

  private val anchorDistanceMap = Map(
    4 -> 1.43,
    6 -> 1.75
  )

  private val boardPosition = Point(level.width / 2, level.height / 3)

  private val board = level.board + boardPosition

  private val boardAnchors: mutable.Map[Point, Option[Int]] = {
    val map = mutable.Map.empty[Point, Option[Int]]
    level.board.anchors.foreach(a => map.put(a + boardPosition, Some(-1)))
    map
  }

  private val restAnchors = {
    val dirs = GridBuilder.buildDirections(board.form).toArray.transform(v => v * 0.5).toList
    val anchors = ListBuffer(board.anchors.head)
    var index = 0
    while (index < anchors.length) {
      dirs.foreach(v => addAnchor(anchors(index) + v, anchors))
      index += 1
    }

    val minDistance = Math.pow(anchorDistanceMap(board.form), 2)
    board.anchors.foreach(boardAnchor => {
      anchors.foreach(freeAnchor => {
        if (freeAnchor.distanceSquareTo(boardAnchor) < minDistance) {
          anchors -= freeAnchor
        }
      })
    })

    val map = mutable.Map.empty[Point, Option[Int]]
    for (a <- anchors) {
      map.put(a, None)
    }

    map
  }

  private val blocks = ListBuffer.empty[BlockExtended]
  private val mid = Point(level.width / 2, level.height / 2)
  for (grid <- level.blocks) {
    blocks += BlockExtended(grid, mid)
    anchorBlock(blocks.size - 1)
  }

  boardAnchors.transform((k, v) => None)


  private var running = true
  private val startTime = System.currentTimeMillis

  private def addAnchor(p: Point, anchors: ListBuffer[Point]): Unit = {
    val border = anchorDistanceMap(level.board.form)
    if (p.x < border || p.x > level.width - border) {
      return
    }
    if (p.y < border || p.y > level.height - border) {
      return
    }
    for (a <- anchors if a.distanceSquareTo(p) < 1e-5) {
      return
    }
    anchors += p
  }

  private def anchorBlock(index: Int): Unit = {
    freeAnchorsWithIndex(index, boardAnchors)
    freeAnchorsWithIndex(index, restAnchors)

    for (i <- blocks.indices if i != index) {
      blockAnchorsAround(i)
    }

    val anchored = anchorOnBoard(index)
    if (!anchored) {
      anchorOnRest(index)
      blockAnchorsAround(index)
    }
  }

  private def checkLevelFinished(): Unit = {
    if (!boardAnchors.values.exists(_.isEmpty)) {
      running = false
      val time = (System.currentTimeMillis - startTime).toInt
      context.parent ! ServerMsg.LevelFinished(time)
    }
  }

  private def anchorOnBoard(index: Int): Boolean = {
    val point = blocks(index).gridExt.anchors.head + blocks(index).position
    val boardAnchor = findNextAnchor(point, getFreeAnchors(boardAnchors), 1)
    if (boardAnchor.isEmpty) {
      return false
    }
    anchorOnAnchor(boardAnchor.get, index, boardAnchors)
  }

  def anchorOnRest(index: Int): Unit = {
    val restList = getFreeAnchors(restAnchors)
    while (restList.nonEmpty) {
      val anchor = findNextAnchor(
        blocks(index).gridExt.anchors.head + blocks(index).position,
        restList)
      assert(anchor.isDefined)
      restList -= anchor.get

      val anchored = anchorOnAnchor(anchor.get, index, restAnchors)
      if (anchored) {
        return
      }
    }
  }

  def anchorOnAnchor(anchor: Point, blockIndex: Int, anchorMap: mutable.Map[Point, Option[Int]]): Boolean = {
    val point = blocks(blockIndex).gridExt.anchors.head + blocks(blockIndex).position
    val diff = Vector.stretch(point, anchor)
    val blockCopy = blocks(blockIndex).copy(
      position = blocks(blockIndex).position + diff
    )

    for (blockAnchor <- blockCopy.gridExt.anchors) {
      val boardAnchor = findNextAnchor(blockAnchor + blockCopy.position, getFreeAnchors(anchorMap), 1e-3)
      if (boardAnchor.isEmpty || anchorMap(boardAnchor.get).isDefined) {
        freeAnchorsWithIndex(blockIndex, anchorMap)
        return false
      }
      anchorMap(boardAnchor.get) = Some(blockIndex)

    }
    blocks(blockIndex) = blockCopy
    true
  }

  def freeAnchorsWithIndex(index: Int, anchorMap: mutable.Map[Point, Option[Int]]): Unit = {
    for ((k, v) <- anchorMap) {
      if (v.isDefined && index == v.get) {
        anchorMap(k) = None
      }
    }
  }

  def findNextAnchor(point: Point,
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

  def blockAnchorsAround(index: Int): Unit = {
    val anchors = blocks(index).gridExt.anchors.toArray.transform(p => p + blocks(index).position).toList
    val minDistanceSquare = Math.pow(anchorDistanceMap(level.board.form), 2)
    for (anchor <- anchors) {
      for ((k, v) <- restAnchors) {
        if (v.isEmpty && anchor.distanceSquareTo(k) < minDistanceSquare) {
          restAnchors(k) = Some(index)
        }
      }
    }
  }

  private def doBlockAction(index: Int)(function: => Unit): Unit = {
    if (!running) {
      log.warning("Action while level is finished")
    } else if (blocks.lift(index).isEmpty) {
      log.error("Invalid block index: " + index)
    } else {
      function
      anchorBlock(index)
      context.parent ! ServerMsg.UpdateBlock(index, blocks(index).block)
      checkLevelFinished()
    }
  }

  override def receive = {

    case InternalMsg.GetGame =>
      val b = ListBuffer.empty[Block]
      for (block <- blocks) {
        b += block.block
      }
      sender ! Game(
        level.name,
        level.width,
        level.height,
        level.board.form,
        board.grid,
        b.toList
      )

    case ClientMsg.UpdateBlockPosition(index, position) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          position = position
        )
      }

    case ClientMsg.RotateBlockLeft(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.rotate(-Math.PI * 2 / level.board.form)
        )
      }

    case ClientMsg.RotateBlockRight(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.rotate(Math.PI * 2 / level.board.form)
        )
      }

    case ClientMsg.MirrorBlockVertical(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.mirrorVertical()
        )
      }

    case ClientMsg.MirrorBlockHorizontal(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.mirrorHorizontal()
        )
      }

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  override def postStop = {
    log.debug("Stopping")
  }

}
