package persistence

import akka.actor.{Actor, ActorLogging}
import model.element.Level
import persistence.LevelManagerNew.{LevelNotFoundException, Load, Save}

import scala.collection.mutable
import scala.util.Try


object LevelManagerNew {
  case class Load(name: String)
  case class LevelNotFoundException() extends RuntimeException

  case class Save(plan: Object) // TODO
  case object LevelAlreadyExistsException extends RuntimeException
}

/**
  * TODO.
  */
class LevelManagerNew extends Actor with ActorLogging{


  private case class MapItem(category: String, level: Option[Level])

  private val levels = mutable.Map.empty[String, MapItem]


  override def receive: Receive = {

    case Load(name) =>
      val mapEntry = levels.get(name)

      if(mapEntry.isEmpty) {
        sender ! Try(LevelNotFoundException())

      } else {
        val level = mapEntry.get.level
        if(level.isDefined) {
          sender ! Try(level.get) // level is cached, return it
        } else {
         // val lv = GridManager.l

          //  TODO neuen Actor erzeugen der Level lädt, forward des sender

        }


      }




    // lädt den Levelplan

    case Save => // TODO save the level
      // add the level here

  }


  // Hält die Pläne für jedes Level, sobald es zum ersten mal angeforedet wird oder geschrieben wird

  // Beim schreiben wird gehalten
  // Beim lesen neu geladen






}
