package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import grizzled.slf4j.Logging
import model.element.LevelKey
import model.element.Plan
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write
import persistence.Persistence
import play.api.mvc.AnyContent
import play.api.mvc.Action
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector


class LevelServiceApplication(implicit injector: Injector) extends Controller with Injectable with Logging {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private val persistence: Persistence = inject[Persistence]

  def index: Action[AnyContent] = readAllKeys

  def createPlan(key: String): Action[AnyContent] = {
    Action.async { request =>
      val key = read[LevelKey](key)
      val plan = read[Plan](request.body.asText.get.toString)
      persistence.createPlan(key, plan).map(_ =>
        Ok
      )
    }
  }

  def readPlan(key: String): Action[AnyContent] = {
    Action.async {
      val key = read[LevelKey](key)
      persistence.readPlan(key).map(plan =>
        Ok(write(plan))
      )
    }
  }

  def deletePlan(key: String): Action[AnyContent] = {
    Action.async {
      persistence.deletePlan(read[LevelKey](key)).map(_ =>
        Ok
      )
    }
  }

  def readAllKeys: Action[AnyContent] = {
    Action.async {
      persistence.readAllKeys().map(keys =>
        Ok(write(keys))
      )
    }
  }


}
