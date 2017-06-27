package persistence

import java.io.{BufferedWriter, File, PrintWriter}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import model.element.LevelKey
import model.element.Plan
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write
import scala.io.Source

import org.json4s.Formats


final class FilePersistence(path: String) extends Persistence {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  new File(path).mkdirs()

  private def createFile(key: LevelKey): File = new File(s"$path/${key.category}/${key.name}.json")

  private def createDir(key: LevelKey): File = new File(s"$path/${key.category}")

  override def createPlan(key: LevelKey, plan: Plan): Future[Unit] = {
    Future {
      val file = createFile(key)
      createDir(key).mkdir()
      if (!file.createNewFile()) {
        throw new IllegalStateException("cant create file, probably it already exists")
      }
      val writer = new BufferedWriter(new PrintWriter(file))
      try {
        writer.write(write(plan))
      } finally {
        writer.close()
      }
    }
  }

  override def readPlan(key: LevelKey): Future[Plan] = {
    Future {
      val file = Source.fromFile(createFile(key))
      try {
        read[Plan](file.mkString)
      } finally {
        file.close()
      }
    }
  }

  override def deletePlan(key: LevelKey): Future[Unit] = {
    Future {
      val file = createFile(key)
      if (!file.delete()) {
        throw new NoSuchElementException("cant delete file, probably it doesn't exists")
      }
      val dir = createDir(key)
      if (dir.list.isEmpty) {
        dir.delete()
      }
    }
  }

  override def readAllKeys(): Future[Set[LevelKey]] = {
    Future {
      new File(path).listFiles.flatMap(cat => cat.listFiles.map(name =>
        LevelKey(cat.getName, name.getName.replace(".json", "")))).toSet
    }
  }

}
