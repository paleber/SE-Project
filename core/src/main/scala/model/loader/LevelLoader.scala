package model.loader

import java.io.File

import model.element.{Level, GridExtended, LevelPlan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object LevelLoader {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private case class MapItem(plan: LevelPlan,
                             level: Option[Level] = None)

  private val levelMap = {
    val dirGrids = new File("core/src/main/resources/levels")
    val map = mutable.Map.empty[String, MapItem]
    for (file <- dirGrids.listFiles()) {
      val source = Source.fromFile(file).mkString
      val plan = read[LevelPlan](source)

      for (b <- plan.variants.indices) {
        val name = file.getName.replace(".json", "") + (b + 'A').toChar
        map.update(name, MapItem(plan))
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

    val blocks = ListBuffer.empty[GridExtended]

    val variant = {
      try {
        item.get.plan.variants(levelName.last - 'A')
      } catch {
        case e: IndexOutOfBoundsException => return None
      }
    }


      for (block <- variant) {
        blocks += GridLoader.load(block)
      }


    val board = GridLoader.buildGrid(item.get.plan.board)


    val level = Some(Level(
      levelName,
      item.get.plan.width,
      item.get.plan.height,
      board,
      blocks.toList
    ))
    levelMap.update(levelName, item.get.copy(level = level))
    level
  }
}


