package persistence

import akka.actor.{Actor, ActorLogging}
import builder.LevelBuilder
import model.element.{Level, LevelId, Plan}
import model.msg.{ClientMsg, ServerMsg}
import persistence.ResourceManager._
import scaldi.{Injectable, Injector}

import scala.util.{Failure, Success, Try}

object ResourceManager {

  case object LoadMenu extends ClientMsg

  case class MenuLoaded(info: Map[String, Seq[String]]) extends ServerMsg

  case class LoadLevel(id: LevelId) extends ClientMsg

  case class LevelLoaded(level: Level) extends ServerMsg


  // TODO
  case class LoadingLevelFailed(id: LevelId) extends ServerMsg

  case class SaveLevel(id: LevelId, plan: Plan) extends ClientMsg

  case class LevelSaved(id: LevelId) extends ServerMsg

  case class LevelAlreadyExists(id: LevelId) extends ServerMsg

}

class ResourceManager(implicit inj: Injector) extends Actor with ActorLogging with Injectable {
  log.debug("Initializing")

  private val persistence = inject[Persistence]

  private def state(info: Map[String, Seq[String]], levels: Map[LevelId, Level]): Receive = {

    case LoadMenu =>
      log.debug("Loading menu")
      sender ! MenuLoaded(info)

    case LoadLevel(id) =>
      val level = levels.get(id)
      if (level.isDefined) {
        log.debug("Loading level from cache: " + id)
        sender ! LevelLoaded(level.get)
      } else {

        Try(persistence.loadPlan(id)) match {
          case Success(plan) =>
            log.debug("Loading level from persistence: " + id)
            val level = LevelBuilder.build(id, plan)
            sender ! LevelLoaded(level)
            context.become(state(info, levels.updated(id, level)))

          case Failure(f) =>
            log.error(s"Loading level from persistence failed: $id ($f)")
            sender ! LoadingLevelFailed(id)
        }

      }

  }

  override def receive: Receive = state(convertIdList(persistence.loadIds), Map.empty)

  private def convertIdList(ids: Seq[LevelId]): Map[String, Seq[String]] = {
    ids.map(_.category).distinct.map(cat => (cat, ids.filter(_.category == cat).map(_.name))).toMap
  }

}
