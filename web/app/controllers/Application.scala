package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import control.MainControl
import gui.Gui
import model.msg.ClientMsg.ShowMenu
import models.Wui
import play.api.http.ContentTypes
import play.api.libs.Comet
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


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

  def comet = Action {
    Ok(views.html.comet())
  }

  def cometStream() = Action {

    val source = Source
      .actorRef[String](bufferSize = 0, OverflowStrategy.fail)
      .mapMaterializedValue(actor =>  {

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

  def angular = Action {
    /** change the template here to use a different way of compilation and loading of the ts ng2 app.
      * index()  :    does no ts compilation in advance. the ts files are download by the browser and compiled there to js.
      * index1() :    compiles the ts files to individual js files. Systemjs loads the individual files.
      * index2() :    add the option -DtsCompileMode=stage to your sbt task . F.i. 'sbt ~run -DtsCompileMode=stage' this will produce the app as one single js file.
      */
    Ok(views.html.index1())
  }

  def notFound(notFound: String) = Default.notFound

  def other(others: String) = index

}
