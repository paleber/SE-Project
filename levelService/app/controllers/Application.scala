package controllers

import grizzled.slf4j.Logging
import model.element.{LevelKey, Plan}
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import persistence.Persistence
import play.api.mvc.{Action, Controller}
import scaldi.{Injectable, Injector}


class Application(implicit injector: Injector) extends Controller with Injectable with Logging {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private val persistence: Persistence = inject[Persistence]

  def index = Action {
    Redirect(routes.Application.loadIds())
  }

  def loadIds = Action {
    trace("Handle Request: loadIds")
    Ok(write(persistence.readAllKeys.toList))
  }

  def loadPlan(id: String) = Action {
    trace(s"Handle Request: loadPlan($id)")
    val plan = persistence.readPlan(read[LevelKey](id))
    Ok(write(plan))
  }

  def savePlan(id: String) = Action { request =>
    trace(s"Handle Request: savePlan($id)")
    val plan = read[Plan](request.body.asText.get.toString)
    persistence.createPlan(read[LevelKey](id), plan)
    Ok
  }

  def removePlan(id: String) = Action {
    trace(s"Handle Request: removePlan($id)")
    persistence.deletePlan(read[LevelKey](id))
    Ok
  }

}
