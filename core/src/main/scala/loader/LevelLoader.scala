package loader

import java.io.File

import control.AnchorHelper
import model.plan.{GridPlan, LevelPlan}
import model.{Block, Grid, Level, Point}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object LevelLoader {

  implicit val formats = Serialization.formats(NoTypeHints)

  private val dirLevels = new File("core/src/main/resources/levels")
  private val levelMap = mutable.Map.empty[String, Option[Level]]

  for(file <- dirLevels.listFiles) {
    levelMap.put(file.getName.replace(".json", ""), None)
  }

  val LEVEL_NAMES = levelMap.keys.toList.sorted

  def load(levelName: String): Option[Level] = {
    val level = levelMap.get(levelName)
    if(level.isEmpty) {
      return None
    }

    val file = Source.fromFile(s"$dirLevels/$levelName.json")
    val plan = read[LevelPlan](file.mkString)

    val newLevel = Some(createLevel(plan))
    levelMap.update(levelName, newLevel)
    newLevel

  }

  def createLevel(plan: LevelPlan): Level = {
    val board = GridLoader.load(plan.board) + Point(plan.width / 2, plan.height / 3)
    val restAnchors = buildRestAnchors(board.form, board, plan.width, plan.height)

    val mid = Point(plan.width / 2, plan.height / 2)
    val blocks = new Array[Block](plan.blocks.length)

    for (blockIndex <- plan.blocks.indices) {
      val block = Block(GridLoader.load(plan.blocks(blockIndex)), mid)
      assert(block.grid.form == board.form)
      blocks(blockIndex) = block
      AnchorHelper.anchorOnRest(blockIndex, blocks, restAnchors)
      AnchorHelper.blockAnchorsAround(
        blockIndex,
        blocks(blockIndex).grid.anchors.toArray.transform(p => p + blocks(blockIndex).position).toList,
        minAnchorDistanceSquare(board.form),
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





