package models.forms

import play.api.libs.json.Json

case class CommandForm(command: String)

object CommandForm {
  implicit val commandForm = Json.format[CommandForm]
}