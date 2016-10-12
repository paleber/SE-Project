package controllers

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import control.MainControl
import gui.Gui
import model.console.ConsoleInput
import model.msg.ClientMessage
import model.msg.ClientMessage.RegisterView
import models.Wui
import models.forms.Forms
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

  private val wui = system.actorOf(Props[Wui], "wui")
  main ! RegisterView(wui)

  main ! ClientMessage.ShowMenu


  def index = Action {
    Redirect(routes.Application.console())
  }

  def hello(name: String) = Action {
    Ok("Hello " + name)
  }

  def guide = Action {
    Ok(views.html.guide())
  }

  def console = Action.async {
    val future = wui ? Wui.ReadMsgBuffer
    future.mapTo[Wui.MsgBuffer].map { msgBuffer =>
      Ok(views.html.console(msgBuffer.messages))
    }
  }

  def command = Action { implicit request =>
    val command = Forms.command.bindFromRequest.get.command
    wui ! ConsoleInput(command)
    Redirect(routes.Application.console())
  }

}
