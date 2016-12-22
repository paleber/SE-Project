package persistence

import java.io.File

import model.element.{GridExtended, Level, LevelPlan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object LevelManager {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private case class MapItem(plan: LevelPlan,
                             level: Option[Level] = None)

  private val levelMap = {
    val dirGrids = new File("core/src/main/resources/levels")
    val map = mutable.Map.empty[String, MapItem]
    for (file <- dirGrids.listFiles()) {
      val source = Source.fromFile(file).mkString
      val plan = read[LevelPlan](source)
      val name = file.getName.replace(".json", "")
      GridManager.gridMap += ((name, GridManager.MapItem(plan.board)))
      for (b <- plan.variants.indices) {
        map.update(name + (b + 'a').toChar, MapItem(plan))
      }
    }
    map
  }

  val LEVEL_NAMES = levelMap.keys.toList.sorted

  def load(levelName: String): Option[Level] = {
    val item = levelMap.get(levelName)
    if (item.isEmpty) {
      return None
    }

    if (item.get.level.isDefined) {
      return item.get.level
    }

    val variant = {
      try {
        item.get.plan.variants(levelName.last - 'a')
      } catch {
        case e: IndexOutOfBoundsException => return None
      }
    }

    val blocks = ListBuffer.empty[GridExtended]
    for (block <- variant) {
      blocks += GridManager.load(block)
    }

    val name = levelName.substring(0, levelName.length - 1)
    val board = GridManager.load(name)

    val level = Some(Level(
      levelName,
      item.get.plan.size,
      item.get.plan.size * 0.625,
      board,
      blocks.toList
    ))
    levelMap.update(levelName, item.get.copy(level = level))
    level
  }
}


