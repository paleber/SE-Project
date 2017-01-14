package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import control.MainControl
import gui.Gui
import model.msg.ClientMsg.ShowMenu
import models.Wui
import play.api.http.ContentTypes
import play.api.libs.Comet
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


class Application @Inject()(silhouette: Silhouette[DefaultEnv],
                            implicit val system: ActorSystem,
                            implicit val mat: Materializer) extends Controller {

  private implicit val timeout: Timeout = 5.seconds

  def index = silhouette.UserAwareAction {
    Redirect(routes.Application.console())
  }

  def guide = silhouette.UserAwareAction { request =>
    Ok(views.html.guide(request.identity))
  }

  def game = silhouette.UserAwareAction { request =>
    Ok(views.html.game(request.identity))
  }

  def console = silhouette.UserAwareAction { request =>
    Ok(views.html.console(request.identity))
  }

  def scongoSocket = Action {
    Ok(views.html.scongoSocket())
  }

  def scongoMenu = Action {
    Ok(views.html.scongoMenu())
  }

  def scongoGame = Action {
    Ok(views.html.scongoGame())
  }

  def scongoFinish = Action {
    Ok(views.html.scongoFinish())
  }

  def socket: WebSocket = WebSocket.accept[String, String] { _ =>
    val control = system.actorOf(MainControl.props)
    system.actorOf(Gui.props(control))
    ActorFlow.actorRef(socket => Wui.props(control, socket))
  }

  def comet = silhouette.UserAwareAction { request =>
    Ok(views.html.comet(request.identity))
  }

  def cometStream() = Action {

    val source = Source
      .actorRef[String](bufferSize = 0, OverflowStrategy.fail)
      .mapMaterializedValue(actor => {

        val control = system.actorOf(MainControl.props)
        system.actorOf(Gui.props(control))
        system.actorOf(Wui.props(control, actor))
        Future {
          Thread.sleep(100)
          control ! ShowMenu
        }

      })

    Ok.chunked(source via Comet.string("parent.cometPush")).as(ContentTypes.HTML)
  }

  def angular = silhouette.UserAwareAction { request =>
    Ok(views.html.angular(request.identity))
  }

  //def notFound(notFound: String) = Default.notFound

  //def other(others: String) = index

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.Application.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

}
