package persistence

import akka.actor.{Actor, ActorLogging}

import model.builder.GridBuilderNew
import model.builder.GridBuilderNew.ConstructedLevel
import model.element.{Level, LevelId, Plan}
import model.msg.ClientMsg.{LoadLevel, LoadMenu}
import model.msg.PersistenceMessages._
import model.msg.ServerMsg.{LevelLoaded, MenuLoaded}

import scala.util.{Failure, Success, Try}

trait Persistence extends Actor with ActorLogging {

  override final def receive: Receive = {

    case LoadMenu =>
      Try(loadMetaInfo) match {
        case Success(info) =>
          sender ! MenuLoaded(info)
        case Failure(f) =>
          log.error(s"failed to load info ($f)")
      }

    case LoadLevel(id: LevelId) =>
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

  }

  @throws[Exception]
  abstract def loadMetaInfo: Map[String, List[String]]

  @throws[Exception]
  abstract def loadPlan(levelId: LevelId): Plan

}

