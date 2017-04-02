package builder

import model.basic.{Point, Vector}
import model.element.{Grid, Level, Plan}
import model.msg.ClientMsg

import scala.collection.mutable.ListBuffer

object Game {

  case class RotateBlockLeft(index: Int) extends ClientMsg

  case class RotateBlockRight(index: Int) extends ClientMsg

  case class MirrorBlockVertical(index: Int) extends ClientMsg

  case class MirrorBlockHorizontal(index: Int) extends ClientMsg

  case class UpdateBlockPosition(index: Int, position: Point) extends ClientMsg

}


object x extends App {

  val level = LevelBuilder.build(null, Plan(4, List(List(List()))))

  val game = new Game(level, AnchorField(4, 0))

}

class Game(level: Level, field: AnchorField) {

  private val (board, boardAnchors, restAnchors) = {
    val (grid, used, blocked) = anchorGrid(level.board.copy(position = Point(field.width / 2, field.height / 3)), field.anchors)
    (grid, used, field.anchors.filter(a => !blocked.contains(a)))
  }

  // Return (AnchoredGrid, UsedAnchors, BlockedAnchors)
  private def anchorGrid(grid: Grid,
                         anchors: List[Point],
                         maxDistance: Double = Double.PositiveInfinity): (Grid, List[Point], List[Point]) = {

    def anchorGridOnAnchor(grid: Grid, anchor: Point, anchors: List[Point]): (Option[Grid], List[Point]) = {
      val v = Vector.stretch(grid.absolute.anchors.head, anchor)
      val gridAnchors = grid.absolute.anchors.map(_ + v)
      val usedAnchors = ListBuffer.empty[Point]
      gridAnchors.foreach(gridAnchors => {
        val a = anchors.find(_.distanceSquareTo(gridAnchors) < 0.1)
        if (a.isEmpty) {
          return (None, List.empty)
        }
        usedAnchors += a.get
      })
      (Some(grid + v), usedAnchors.toList)
    }

    val maxDistSquare = maxDistance * maxDistance
    anchors.
      filter(_.distanceSquareTo(grid.absolute.anchors.head) < maxDistSquare).
      sortBy(_.distanceSquareTo(grid.absolute.anchors.head)).
      foreach(anchor => {
        val (anchoredGrid, usedAnchors) = anchorGridOnAnchor(grid, anchor, anchors)
        if (anchoredGrid.isDefined) {
          return (grid, usedAnchors, usedAnchors.flatMap(a => field.neighbors(a)).distinct)
        }
      })
    throw new IllegalArgumentException("Anchoring grid failed")
  }

  class Element(var grid: Grid, var blocked: List[Point] = List.empty)

  val elements: List[Element] = level.blocks.map(b => new Element(b))

  // TODO shuffle, rotate and mirror Elements

  elements.indices.foreach(index => anchorBlockOnRest(index, Point(field.width / 2, field.height / 2)))

  def anchorBlockOnRest(index: Int, position: Point): Unit = {
    val blockedRest = elements.flatMap(_.blocked).distinct
    val freeRest = restAnchors.filter(a => !blockedRest.contains(a))



    val (grid, _, blocked) = anchorGrid(elements(index).grid.copy(position = position), freeRest)
    println(index, "asd",grid.position)
    elements(index).grid = grid
    elements(index).blocked = blocked
  }

  def currentState: Level = level.copy(board = board, blocks = elements.map(_.grid))

  /*
  private val boardPosition = Point(level.width / 2, level.height / 3)

  private val board = level.board.absolute

  private val boardAnchors: mutable.Map[Point, Option[Int]] = {
    val map = mutable.Map.empty[Point, Option[Int]]
    level.board.anchors.foreach(a => map.put(a + boardPosition, Some(-1)))
    map
  }

  private val restAnchors = {
    val dirs = GridBuilder.buildDirections(level.form).toArray.transform(v => v * 0.5).toList
    val anchors = ListBuffer(board.anchors.head)
    var index = 0
    while (index < anchors.length) {
      dirs.foreach(v => addAnchor(anchors(index) + v, anchors))
      index += 1
    }

    val minDistance = Math.pow(anchorDistanceMap(level.form), 2)
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

  private val blocks = ListBuffer.empty[Grid]
  private val mid = Point(level.width / 2, level.height / 2)
  for (grid <- level.blocks) {
    blocks += grid.copy(position = mid)
    anchorBlock(blocks.size - 1)
  }

  boardAnchors.transform((k, v) => None)


  private var running = true
  private val startTime = System.currentTimeMillis


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
    val point = blocks(index).anchors.head + blocks(index).position
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
        blocks(index).anchors.head + blocks(index).position,
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
    val point = blocks(blockIndex).anchors.head + blocks(blockIndex).position
    val diff = Vector.stretch(point, anchor)
    val blockCopy = blocks(blockIndex).copy(
      position = blocks(blockIndex).position + diff
    )

    for (blockAnchor <- blockCopy.anchors) {
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
    val anchors = blocks(index).anchors.toArray.transform(p => p + blocks(index).position).toList
    val minDistanceSquare = Math.pow(anchorDistanceMap(level.form), 2)
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
      context.parent ! ServerMsg.UpdateBlock(index, blocks(index))
      checkLevelFinished()
    }
  } */


  /*
  case InternalMsg.GetGame =>
    val b = ListBuffer.empty[Grid]
    for (block <- blocks) {
      b += block
    }
    sender ! Level(
      level.id,
      level.width,
      level.height,
      level.form,
      board,
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
      blocks(index) = blocks(index).rotate(-Math.PI * 2 / level.form)
    }

  case ClientMsg.RotateBlockRight(index) =>
    doBlockAction(index) {
      blocks(index) = blocks(index).rotate(Math.PI * 2 / level.form)
    }

  case ClientMsg.MirrorBlockVertical(index) =>
    doBlockAction(index) {
      blocks(index) = blocks(index).mirrorVertical()
    }

  case ClientMsg.MirrorBlockHorizontal(index) =>
    doBlockAction(index) {
      blocks(index) = blocks(index).mirrorHorizontal()
    }*/


}
