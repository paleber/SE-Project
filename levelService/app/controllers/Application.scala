package controllers

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import persistence.Persistence
import play.api.mvc.{Action, Controller}
import scaldi.{Injectable, Injector}


class Application(implicit injector: Injector) extends Controller with Injectable {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val persistence = inject[Persistence]

  def getIds = Action {
    Ok(write(persistence.loadIds.toList))
  }


}
