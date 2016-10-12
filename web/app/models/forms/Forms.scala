package models.forms

import play.api.data.Form
import play.api.data.Forms._

case class CommandForm(command: String)

case object Forms {

  val command: Form[CommandForm] = Form {
    mapping(
      "command" -> text
    )(CommandForm.apply)(CommandForm.unapply)
  }

}
