package persistence

import java.io.File

import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import persistence.FilePersistence.path

import scala.io.Source

object FilePersistence {

  private val path = "core/src/main/resources/lvNew"

}

final class FilePersistence extends Persistence {

  private implicit val formats = Serialization.formats(NoTypeHints)

  override def loadMetaInfo: Map[String, List[String]] = {
    new File(path).listFiles.map(file =>
      (file.getName, file.listFiles.map(_.getName.replace(".json", "")).toList)).toMap
  }

  override def loadPlan(id: LevelId): Plan = {
    read[Plan](Source.fromFile(new File(s"$path/${id.category}/${id.name}.json")).mkString)
  }

  override def savePlan(levelId: LevelId, plan: Plan): Unit = {
    throw new UnsupportedOperationException("saving into filesystem not allowed")
  }

}
