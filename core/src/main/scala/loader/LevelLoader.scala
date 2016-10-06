package loader

import control.AnchorHelper
import model.{Block, Grid, Level, Point}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object LevelLoader {

  val rotationSteps = 4

  def load(levelIndex: Int): Option[Level] = {

    levelIndex match {

      case 0 =>
        Some(createLevel(LevelPlan(10, 8, 3, List(0, 1, 2))))

      case 1 =>
        Some(createLevel(LevelPlan(12, 9, 1000, List(1001, 1002))))

      case _ => None
    }

  }

  def createLevel(plan: LevelPlan): Level = {
    val board = GridLoader.load(plan.boardId) + Point(plan.width / 2, plan.height / 3)
    val restAnchors = buildRestAnchors(board.rotationSteps, board, plan.width, plan.height)

    val mid = Point(plan.width / 2, plan.height / 2)
    val blocks = new Array[Block](plan.blockIds.length)

    for (blockIndex <- plan.blockIds.indices) {
      val block = Block(GridLoader.load(plan.blockIds(blockIndex)), mid)
      assert(block.grid.rotationSteps == board.rotationSteps)
      blocks(blockIndex) = block
      AnchorHelper.anchorOnRest(blockIndex, blocks, restAnchors)
      AnchorHelper.blockAnchorsAround(
        blockIndex,
        blocks(blockIndex).grid.anchors.toArray.transform(p => p + blocks(blockIndex).position).toList,
        minAnchorDistanceSquare(board.rotationSteps),
        restAnchors
      )
      restAnchors.foreach { case (k, v) => if (v.isDefined) println(k + " - " + v) }
    }

    Level(plan.width, plan.height, board, blocks.toList, restAnchors.keys.toList)
  }

  def minAnchorDistanceSquare(rotationSteps: Int): Double = {
    assert(rotationSteps == 4 || rotationSteps == 6)
    rotationSteps match {
      case 4 => Math.pow(1.49, 2)
      case 6 => Math.pow(1.8, 2)
    }
  }


  private def buildRestAnchors(rotationSteps: Int, board: Grid, width: Double, height: Double): mutable.Map[Point, Option[Int]] = {
    val dirs = GridLoader.buildDirections(rotationSteps).toArray.transform(v => v * 0.5).toList
    val anchors = ListBuffer(board.anchors.head)
    var index = 0
    while (index < anchors.length) {
      dirs.foreach(v => addAnchor(anchors(index) + v, anchors, width, height))
      index += 1
    }

    val minDistanceSquare = minAnchorDistanceSquare(rotationSteps)

    board.anchors.foreach(boardAnchor => {
      anchors.foreach(freeAnchor => {
        if (freeAnchor.distanceSquareTo(boardAnchor) < minDistanceSquare) {
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

  private def addAnchor(p: Point, anchors: ListBuffer[Point], width: Double, height: Double): Unit = {
    if (p.x < 0.99 || p.x > width - 0.99) {
      return
    }
    if (p.y < 0.99 || p.y > height - 0.99) {
      return
    }
    anchors.foreach(a =>
      if (a.distanceSquareTo(p) < 1e-5) {
        return
      }
    )
    anchors += p
  }

}


case class LevelPlan(width: Double,
                     height: Double,
                     boardId: Int,
                     blockIds: List[Int])


