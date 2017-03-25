package persistence

import java.io.{File, FileNotFoundException}

import akka.actor.{Actor, ActorSystem, Props}
import model.builder.GridBuilderNew
import model.builder.GridBuilderNew.ConstructedLevel
import model.element.Level
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import persistence.ResourceLoader._

import scala.io.Source

/**
  * TODO.
  */

object ResourceLoader {

  case object LoadLevelInfo

  case class LevelInfo(info: Map[String, Array[String]])

  case class LoadLevel(category: String, name: String)


  private val path = "core/src/main/resources/lvNew"


  case class NewLevelPlan(form: Int, shifts: List[List[List[Int]]])

}

class ResourceLoader extends Actor {

  private implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: Receive = {

    case LoadLevelInfo =>
      sender ! LevelInfo(new File(path).listFiles.map(file =>
        (file.getName, file.listFiles.map(_.getName.replace(".json", "")))).toMap)

    case LoadLevel(category, name) =>
      try {
        val source = Source.fromFile(new File(s"$path/$category/$name.json")).mkString
        val plan = read[NewLevelPlan](source)
        val ConstructedLevel(board, blocks, width, height) = GridBuilderNew.build(plan)

        sender ! Some(Level(
          category = category,
          name = name,
          width = width,
          height = height,
          form = plan.form,
          board = board,
          blocks = blocks))

      } catch {
        case _: FileNotFoundException => sender ! None
      }
  }

}

object x extends App {

  private val system = ActorSystem("scongo")

  private val main = system.actorOf(Props[ResourceLoader], "main")


  main ! LoadLevel("asd", "lv2")

}