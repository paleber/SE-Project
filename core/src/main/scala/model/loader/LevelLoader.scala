package model.loader

import java.io.File

import control.ExtendedLevel
import model.element.Grid
import model.plan.LevelPlan
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

object LevelLoader {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val dirLevels = new File("core/src/main/resources/levels")
  private val levelMap = mutable.Map.empty[String, Option[ExtendedLevel]]

  for(file <- dirLevels.listFiles) {
    levelMap.put(file.getName.replace(".json", ""), None)
  }

  val LEVEL_NAMES = levelMap.keys.toList.sorted

  def load(levelName: String): Option[ExtendedLevel] = {
    val level = levelMap.get(levelName)
    if(level.isEmpty) {
      return None
    }

    val file = Source.fromFile(s"$dirLevels/$levelName.json")
    val plan = read[LevelPlan](file.mkString)


    val board = GridLoader.load(plan.board)
    val blocks = ListBuffer.empty[Grid]
    for(block <- plan.blocks) {
      blocks += GridLoader.load(block)
    }

    val newLevel = Some(ExtendedLevel(
      levelName,
      plan.width,
      plan.height,
      board,
      blocks.toList
    ))
    levelMap.update(levelName, newLevel)
    newLevel

  }

}


