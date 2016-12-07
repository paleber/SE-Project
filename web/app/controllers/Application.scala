package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.Timeout
import control.MainControl
import gui.Gui
import models.Wui
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.duration._

class Application @Inject()(implicit system: ActorSystem, mat: Materializer) extends Controller {

  private implicit val timeout: Timeout = 5.seconds

  def index = Action {
    Redirect(routes.Application.console())
  }

  def guide = Action {
    Ok(views.html.guide())
  }

  def game = Action {
    Ok(views.html.game())
  }

  def console = Action {
    Ok(views.html.console())
  }

  def scongoBoard = Action {
    Ok(views.html.scongoBoard())
  }

  def socket: WebSocket = WebSocket.accept[String, String] { _ =>
    val control = system.actorOf(MainControl.props)
    system.actorOf(Gui.props(control))
    ActorFlow.actorRef(socket => Wui.props(control, socket))
  }

}
