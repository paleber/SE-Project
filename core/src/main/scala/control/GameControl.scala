package control

import akka.actor.{Actor, ActorLogging}
import model.basic.{Point, Vector}
import model.element.{Block, Game, BlockExtended, Level}
import model.loader.GridLoader
import model.msg.{ClientMessage, InternalMessage, ServerMessage}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


class GameControl(game: Game) extends Actor with ActorLogging {
  log.debug("Initializing")

  private val anchorDistanceMap = Map(
    4 -> Math.pow(1.49, 2),
    6 -> Math.pow(1.8, 2)
  )

  private val boardPosition = Point(game.width / 2, game.height / 3)

  private val board = game.board + boardPosition

  private val boardAnchors: mutable.Map[Point, Option[Int]] = {
    val map = mutable.Map.empty[Point, Option[Int]]
    game.board.anchors.foreach(a => map.put(a + boardPosition, Some(-1)))
    map
  }

  private val restAnchors = {
    val dirs = GridLoader.buildDirections(board.form).toArray.transform(v => v * 0.5).toList
    val anchors = ListBuffer(board.anchors.head)
    var index = 0
    while (index < anchors.length) {
      dirs.foreach(v => addAnchor(anchors(index) + v, anchors))
      index += 1
    }

    board.anchors.foreach(boardAnchor => {
      anchors.foreach(freeAnchor => {
        if (freeAnchor.distanceSquareTo(boardAnchor) < anchorDistanceMap(board.form)) {
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
  private val mid = Point(game.width / 2, game.height / 2)
  for (grid <- game.blocks) {
    blocks += BlockExtended(grid, mid)
    anchorBlock(blocks.size - 1)
  }

  boardAnchors.transform((k, v) => None)


  private var running = true
  private val startTime = System.currentTimeMillis

  private def addAnchor(p: Point, anchors: ListBuffer[Point]): Unit = {
    if (p.x < 0.99 || p.x > game.width - 0.99) {
      return
    }
    if (p.y < 0.99 || p.y > game.height - 0.99) {
      return
    }
    for(anchor <- anchors) {
      if (anchor.distanceSquareTo(p) < 1e-5) {
        return
      }
    }
    anchors += p
  }


  private def anchorBlock(index: Int): Unit = {
    freeAnchorsWithIndex(index, boardAnchors)
    freeAnchorsWithIndex(index, restAnchors)

    for (i <- blocks.indices if i != index) {
      blockAnchorsAround(
        i,
        blocks(i).gridExt.anchors.toArray.transform(p => p + blocks(i).position).toList,
        anchorDistanceMap(game.board.form),
        restAnchors)
    }

    val anchored = anchorOnBoard(index)
    if (!anchored) {
      anchorOnRest(index)
      blockAnchorsAround(index,
        blocks(index).gridExt.anchors.toArray.transform(p => p + blocks(index).position).toList,
        anchorDistanceMap(game.board.form),
        restAnchors)
    }


  }


  private def checkLevelFinished(): Unit = {
    if (!boardAnchors.values.exists(_.isEmpty)) {
      running = false
      val time = (System.currentTimeMillis - startTime).toInt
      context.parent ! ServerMessage.LevelFinished(time)
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
    anchorMap.foreach { case (k, v) =>
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


  private def doBlockAction(index: Int)(function: => Unit): Unit = {
    if (!running) {
      log.warning("Action while level is finished")
    } else if (blocks.lift(index).isEmpty) {
      log.error("Invalid block index: " + index)
    } else {
      function
      anchorBlock(index)
      context.parent ! ServerMessage.UpdateBlock(index, blocks(index).block)
      checkLevelFinished()
    }
  }

  override def receive = {

    case InternalMessage.GetGame =>
      val b = ListBuffer.empty[Block]
      for (block <- blocks) {
        b += block.block
      }
      sender ! Level(
        game.name,
        game.width,
        game.height,
        game.board.form,
        board.grid,
        b.toList
      )

    case ClientMessage.UpdateBlockPosition(index, position) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          position = position
        )
      }

    case ClientMessage.RotateBlockLeft(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.rotate(-Math.PI * 2 / game.board.form)
        )
      }

    case ClientMessage.RotateBlockRight(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.rotate(Math.PI * 2 / game.board.form)
        )
      }

    case ClientMessage.MirrorBlockVertical(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.mirrorVertical()
        )
      }

    case ClientMessage.MirrorBlockHorizontal(index) =>
      doBlockAction(index) {
        blocks(index) = blocks(index).copy(
          gridExt = blocks(index).gridExt.mirrorHorizontal()
        )
      }

    case msg => log.warning("Unhandled message: " + msg)
  }


  override def postStop = {
    log.debug("Stopping")
  }

}
