package controllers

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import control.MainControl
import gui.Gui
import model.msg.ClientMessage
import model.msg.ClientMessage.RegisterView
import models.Wui
import models.forms.CommandForm
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import tui.Tui

import scala.concurrent.duration._

class Application extends Controller {

  implicit val timeout: Timeout = 5.seconds

  private val system = ActorSystem()
  private val main = system.actorOf(Props[MainControl], "control")

  main ! RegisterView(system.actorOf(Props[Tui], "tui"))
  main ! RegisterView(system.actorOf(Props[Gui], "gui"))

  private val wuiConsole = system.actorOf(Props[Wui], "console")
  main ! RegisterView(wuiConsole)

  main ! ClientMessage.ShowMenu

  val commandForm: Form[CommandForm] = Form {
    mapping(
      "command" -> text
    )(CommandForm.apply)(CommandForm.unapply)
  }

  def index = Action {
    Ok("Hello world")
  }

  def hello(name: String) = Action {
    Ok("Hello " + name)
  }

  def console = Action.async {
    val future = wuiConsole ? Wui.ReadMsgBuffer
    future.mapTo[Wui.MsgBuffer].map { msgBuffer =>
      Ok(views.html.hello(msgBuffer.messages))
    }
  }

  def command = Action { implicit request =>

    print(request)
    val command = commandForm.bindFromRequest.get



    print("" + command.command)

    Ok("" + command.command)
  }

}
