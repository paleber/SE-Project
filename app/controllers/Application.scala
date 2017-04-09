package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.Wui
import module.ScongoModule
import play.api.Mode
import play.api.http.ContentTypes
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Comet
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import scaldi.Injector
import scaldi.akka.AkkaInjectable
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.language.postfixOps


class Application @Inject()(silhouette: Silhouette[DefaultEnv],
                            val messagesApi: MessagesApi,
                            socialProviderRegistry: SocialProviderRegistry,
                            implicit val env: play.api.Environment,
                            implicit val mat: Materializer,
                            implicit val webJarAssets: WebJarAssets) extends Controller with I18nSupport with AkkaInjectable{

  private implicit val injector: Injector = ScongoModule

  private implicit val system = inject[ActorSystem]

  private implicit val timeout: Timeout = 5.seconds

  def index = silhouette.UserAwareAction {
    Redirect(routes.Application.game())
  }

  def guide = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.guide(request.identity))
  }

  def game = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.game(request.identity))
  }

  def console = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.console(request.identity))
  }

  def scongoSocket = Action { implicit request =>
    Ok(views.html.scongoSocket())
  }

  def scongoMenu = Action { implicit request =>
    Ok(views.html.scongoMenu())
  }

  def scongoGame = Action { implicit request =>
    Ok(views.html.scongoGame())
  }

  def scongoFinish = Action { implicit request =>
    Ok(views.html.scongoFinish())
  }

  def scongoLevelButton = Action { implicit request =>
    Ok(views.html.scongoLevelButton())
  }

  def socket: WebSocket = WebSocket.accept[String, String] { _ =>
    ActorFlow.actorRef(socket => Wui.props(socket, env.mode == Mode.Prod))
  }

  def comet = silhouette.UserAwareAction { implicit request =>
    Ok(views.html.comet(request.identity))
  }

  def cometStream() = Action {
    val source = Source
      .actorRef[String](bufferSize = 0, OverflowStrategy.fail)
      .mapMaterializedValue(stream => {
        system.actorOf(Wui.props(stream, env.mode == Mode.Prod))
      })
    Ok.chunked(source via Comet.string("parent.cometPush")).as(ContentTypes.HTML)
  }

  def angular = silhouette.UserAwareAction {
    implicit request =>
      Ok(views.html.angular(request.identity))
  }

  def angularSub(path: String) = silhouette.UserAwareAction {
    implicit request =>
      Ok(views.html.angular(request.identity))
  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut: Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request =>
      val result = Redirect(routes.Application.index())
      silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
      silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

}
