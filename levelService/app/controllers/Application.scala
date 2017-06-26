package controllers

import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import grizzled.slf4j.Logging
import model.element.{LevelKey, Plan}
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import persistence.Persistence
import play.api.mvc.AnyContent
import play.api.mvc.{Action, Controller}
import scaldi.{Injectable, Injector}


class Application(implicit injector: Injector) extends Controller with Injectable with Logging {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private val persistence: Persistence = inject[Persistence]

  def index: Action[AnyContent] = loadIds

  def loadIds: Action[AnyContent] = {
    Action.async {
      trace("Handle Request: loadIds")
      persistence.readAllKeys().map(keys =>
        Ok(write(keys))
      )
    }
  }

  def loadPlan(id: String): Action[AnyContent] = {
    Action.async {
      trace(s"Handle Request: loadPlan($id)")
      persistence.readPlan(read[LevelKey](id)).map(plan =>
        Ok(write(plan))
      )
    }
  }

  def savePlan(id: String): Action[AnyContent] = {
    Action { request =>
      trace(s"Handle Request: savePlan($id)")
      val plan = read[Plan](request.body.asText.get.toString)
      Await.result(persistence.createPlan(read[LevelKey](id), plan).map(_ => Ok), Duration(10, TimeUnit.SECONDS))
      Ok
    }
  }

  def removePlan(id: String): Action[AnyContent] = {
    Action {
      trace(s"Handle Request: removePlan($id)")
      Await.result(persistence.deletePlan(read[LevelKey](id)), Duration(10, TimeUnit.SECONDS))
      Ok
    }
  }

}
