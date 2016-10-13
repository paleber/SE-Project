package persistence

import java.io.File

import model.builder.GridBuilder
import model.element.{GridExtended, GridPlan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read

import scala.collection.mutable
import scala.io.Source


private[persistence] object GridManager {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private[persistence] case class MapItem(plan: GridPlan,
                                          grid: Option[GridExtended] = None)

  private[persistence] val gridMap = {
    val dirGrids = new File("core/src/main/resources/blocks")
    val map = mutable.Map.empty[String, MapItem]
    for (file <- dirGrids.listFiles()) {
      val source = Source.fromFile(file).mkString
      val plan = read[GridPlan](source)
      val name = file.getName.replace(".json", "")
      map += ((name, MapItem(plan)))
    }
    map
  }

  private[persistence] def load(name: String): GridExtended = {
    assert(gridMap.contains(name))
    val item = gridMap(name)
    if (item.grid.isDefined) {
      return item.grid.get
    }
    val grid = GridBuilder.build(item.plan)
    gridMap.update(name, item.copy(grid = Some(grid)))
    grid
  }

}
