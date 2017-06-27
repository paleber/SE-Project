package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import model.element.LevelKey
import model.element.Plan
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write
import persistence.Persistence
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Controller
import scaldi.Injectable
import scaldi.Injector


class PlanService(implicit injector: Injector) extends Controller with Injectable {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private val persistence: Persistence = inject[Persistence]

  def createPlan(key: String): Action[AnyContent] = {
    Action.async { request =>
      persistence.createPlan(
        read[LevelKey](key),
        read[Plan](request.body.asText.get.toString)
      ).map(_ =>
        Ok
      )
    }
  }

  def readPlan(key: String): Action[AnyContent] = {
    Action.async {
      persistence.readPlan(
        read[LevelKey](key)
      ).map(plan =>
        Ok(write(plan))
      )
    }
  }

  def deletePlan(key: String): Action[AnyContent] = {
    Action.async {
      persistence.deletePlan(
        read[LevelKey](key)
      ).map(_ =>
        Ok
      )
    }
  }

  def readKeys: Action[AnyContent] = {
    Action.async {
      persistence.readAllKeys().map(keys =>
        Ok(write(keys))
      )
    }
  }

}
