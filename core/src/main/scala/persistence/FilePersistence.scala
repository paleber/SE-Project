package persistence

import java.io.{BufferedWriter, File, PrintWriter}

import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import scaldi.{Injectable, Injector}

import scala.io.Source


final class FilePersistence(implicit inj: Injector) extends Persistence with Injectable {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val path = inject[String]('filePath)
  new File(path).mkdirs()

  private def createFile(id: LevelId): File = new File(s"$path/${id.category}/${id.name}.json")

  private def createDir(id: LevelId): File = new File(s"$path/${id.category}")

  override def loadPlan(id: LevelId): Plan = {
    val file = Source.fromFile(createFile(id))
    try {
      read[Plan](file.mkString)
    } finally {
      file.close()
    }
  }

  override def savePlan(id: LevelId, plan: Plan): Unit = {
    val file = createFile(id)
    createDir(id).mkdir()
    if (!file.createNewFile()) {
      throw new IllegalStateException("cant create file, maybe it exists already")
    }
    val writer = new BufferedWriter(new PrintWriter(file))
    try {
      writer.write(write(plan))
    } finally {
      writer.close()
    }
  }

  override def loadIds: Seq[LevelId] = {
    new File(path).listFiles.flatMap(cat => cat.listFiles.map(name =>
      LevelId(cat.getName, name.getName.replace(".json", "")))).toList
  }

  override def removePlan(id: LevelId): Unit = {
    val file = createFile(id)
    if (!file.delete()) {
      throw new NoSuchElementException("cant delete file, maybe it does not exist")
    }
    val dir = createDir(id)
    if (dir.list.isEmpty) {
      dir.delete()
    }
  }

}
