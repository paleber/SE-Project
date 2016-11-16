package controllers

import javax.inject.Inject

import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import akka.util.Timeout
import control.MainControl
import gui.Gui
import model.console.ConsoleInput
import model.msg.{ClientMsg, ScongoMsg}
import models.Wui
import models.forms.Forms
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.duration._

class Application @Inject()(implicit mat: Materializer) extends Controller {

  private implicit val timeout: Timeout = 5.seconds
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ScongoMsg])))

  private implicit val system = ActorSystem()


  def index = Action {
    Redirect(routes.Application.console())
  }

  def hello(name: String) = Action {
    Ok("Hello " + name)
  }

  def guide = Action {
    Ok(views.html.guide())
  }

  def game = Action {
    Ok(views.html.game())
  }

  def console = Action {
    Ok("TODO")
  }

  /*
  def console = Action.async {
    val future = wui ? Wui.ReadMsgBuffer
    future.mapTo[Wui.MsgBuffer].map { msgBuffer =>
      Ok(views.html.console(msgBuffer.messages))
    }
  }*/

  def level(name: String) = Action {

    //main ! ClientMsg.ShowGame(name)
    Ok(name)
  }

  def command = Action { implicit request =>
    val command = Forms.command.bindFromRequest.get.command
    // wui ! ConsoleInput(command) // TODO
    Redirect(routes.Application.console())
  }

  def loadState = Action {
    Ok("TODO")
  }

  import play.api.mvc._
  import play.api.libs.streams._


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => MainControl.props(Map(
      "gui" -> Props[Gui],
      "wui" -> Wui.props(out)
    )))
  }


  /*def loadState = Action.async {
    val future = wui ? Wui.ReadMsgBuffer
    future.mapTo[Wui.MsgBuffer].map { msgBuffer =>
      Ok(Serialization.write(msgBuffer.messages))
    }
  }*/

}
