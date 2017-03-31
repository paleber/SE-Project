package persistence

import akka.actor.{Actor, ActorLogging}
import model.builder.GridBuilderNew
import model.builder.GridBuilderNew.ConstructedLevel
import model.element.{Level, LevelId, Plan}
import model.msg.PersistenceMessages._
import model.msg.{ClientMsg, ServerMsg}
import persistence.Persistence.{LevelLoaded, LoadGame, LoadMenu, MenuLoaded}

import scala.util.{Failure, Success, Try}

object Persistence {

  case object LoadMenu extends ClientMsg

  case class LoadGame(id: LevelId) extends ClientMsg

  case class MenuLoaded(info: Map[String, List[String]]) extends ServerMsg

  case class LevelLoaded(level: Level) extends ServerMsg

}

trait Persistence extends Actor with ActorLogging {

  override final def receive: Receive = {

    case LoadMenu =>
      log.debug("Loading menu")
      Try(loadMetaInfo) match {
        case Success(info) =>
          sender ! MenuLoaded(info)
        case Failure(f) =>
          log.error(s"failed to load info ($f)")
      }

    case LoadGame(id: LevelId) =>
      log.debug("Loading level - " + id)
      Try(loadPlan(id)) match {
        case Success(plan) =>
          val ConstructedLevel(board, blocks, width, height) = GridBuilderNew.build(plan)
          sender ! LevelLoaded(
            Level(
              id = id,
              width = width,
              height = height,
              form = plan.form,
              board = board,
              blocks = blocks)
          )
        case Failure(f) =>
          log.error(s"failed to load plan ($f)")
          sender ! LoadingLevelFailed(id)
      }

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  @throws[Exception]
  def loadMetaInfo: Map[String, List[String]]

  @throws[Exception]
  def loadPlan(levelId: LevelId): Plan

}

