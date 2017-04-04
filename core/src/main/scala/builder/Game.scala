package builder

import model.basic.{Point, Vector}
import model.element.{Grid, Level}
import model.msg.{ClientMsg, ServerMsg}

import scala.collection.mutable.ListBuffer
import scala.util.{Random, Try}

object Game {

  case class RotateBlockLeft(index: Int) extends ClientMsg

  case class RotateBlockRight(index: Int) extends ClientMsg

  case class MirrorBlockVertical(index: Int) extends ClientMsg

  case class MirrorBlockHorizontal(index: Int) extends ClientMsg

  case class UpdateBlockPosition(index: Int, position: Point) extends ClientMsg


  case class BlockUpdated(index: Int, block: Grid) extends ServerMsg

  case class LevelFinished(timeMillis: Int) extends ServerMsg
}

class Game(level: Level, field: AnchorField) {

  private val mid = Point(field.width / 2, field.height / 2)

  private val (board, boardAnchors, restAnchors) = {
    val (grid, used, blocked) = anchorGrid(
      level.board.copy(position = Point(field.width / 2, field.height / 3)),
      field.anchors, isBoard = true)
    (grid, used, field.anchors.filter(a => !blocked.contains(a)))
  }

  // returns (AnchoredGrid, UsedAnchors, BlockedAnchors)
  private def anchorGrid(grid: Grid,
                         anchors: List[Point],
                         maxDistance: Double = Double.PositiveInfinity,
                         isBoard: Boolean = false): (Grid, List[Point], List[Point]) = {

    def anchorGridOnAnchor(anchor: Point): (Option[Grid], List[Point]) = {
      val v = Vector.stretch(grid.absolute.anchors.head, anchor)
      if (isBoard && Math.abs(v.x) > 0.1) {
        return (None, List.empty)
      }
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
        val (anchoredGrid, usedAnchors) = anchorGridOnAnchor(anchor)
        if (anchoredGrid.isDefined) {
          return (anchoredGrid.get, usedAnchors, usedAnchors.flatMap(a => field.neighbors(a)).distinct)
        }
      })
    throw new IllegalArgumentException("Anchoring grid failed")
  }

  class Element(var grid: Grid, var blocked: List[Point] = List.empty)

  private val elements: List[Element] = Random.shuffle(level.blocks.map(b =>
    new Element(
      b.copy(position = mid).
        mirrorVertical(Random.nextInt(1)).
        rotate(Random.nextInt(level.form) * (Math.PI * 2 / level.form))
    )
  ))

  elements.indices.foreach(index => anchorBlockOnRest(index))

  private val startTime = System.currentTimeMillis

  def anchorBlockOnRest(index: Int): Unit = {
    val blockedRest = elements.filter(e => elements.indexOf(e) != index).flatMap(_.blocked).distinct
    val freeRest = restAnchors.filter(a => !blockedRest.contains(a))

    val (grid, _, blocked) = anchorGrid(elements(index).grid, freeRest)

    elements(index).grid = grid
    elements(index).blocked = blocked
  }

  def anchorBlockOnBoard(index: Int): Unit = {
    val blockedBoard = elements.filter(e => elements.indexOf(e) != index).flatMap(_.blocked).distinct
    val freeBoard = boardAnchors.filter(a => !blockedBoard.contains(a))

    val (grid, used, _) = anchorGrid(elements(index).grid, freeBoard, field.maxNeighborDistance / 2)

    elements(index).grid = grid
    elements(index).blocked = used
  }

  def currentState: Level = level.copy(board = board, blocks = elements.map(_.grid))


  def updateBlockPosition(index: Int, position: Point): (Grid, Option[Int]) = {
    elements(index).grid = elements(index).grid.copy(position = position)
    anchorBlock(index)
  }

  def rotateBlockLeft(index: Int): (Grid, Option[Int]) = {
    elements(index).grid = elements(index).grid.rotate(-Math.PI * 2 / level.form)
    anchorBlock(index)
  }

  def rotateBlockRight(index: Int): (Grid, Option[Int]) = {
    elements(index).grid = elements(index).grid.rotate(Math.PI * 2 / level.form)
    anchorBlock(index)
  }

  def mirrorBlockHorizontal(index: Int): (Grid, Option[Int]) = {
    elements(index).grid = elements(index).grid.mirrorHorizontal()
    anchorBlock(index)
  }

  def mirrorBlockVertical(index: Int): (Grid, Option[Int]) = {
    elements(index).grid = elements(index).grid.mirrorVertical()
    anchorBlock(index)
  }

  private def anchorBlock(index: Int): (Grid, Option[Int]) = {
    val element = elements(index)
    element.blocked = List.empty

    if (Try(anchorBlockOnBoard(index)).isSuccess) {
      val blockedBoard = elements.flatMap(_.blocked)
      if (boardAnchors.exists(a => !blockedBoard.contains(a))) {
        return (element.grid, None)
      }
      return (element.grid, Some((System.currentTimeMillis - startTime).toInt))
    }

    if (Try(anchorBlockOnRest(index)).isSuccess) {
      return (element.grid, None)
    }

    // fallback (should normally not happen): if anchoring completely fails, just move the block to the mid
    element.grid = element.grid.copy(position = mid)
    element.blocked = List.empty
    (element.grid, None)
  }

}
