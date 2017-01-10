package controllers

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import control.MainControl
import gui.Gui
import models.Wui
import play.api.http.ContentTypes
import play.api.libs.Comet
import play.api.libs.json.{JsString, JsValue}
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
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

  def consoleComet = Action {
    Ok(views.html.consoleComet())
  }

  def comet = Action {
    implicit val m = mat // TODO needed here again?
    //def jsonSource: Source[JsValue, _] = Source(List(JsString("jsonString")))

    println("Comet")

    val s = Source
      .actorRef[String](bufferSize = 0, OverflowStrategy.dropHead)
      .mapMaterializedValue(actor => {
        actor ! "ABC"
        /*println("Future")
        Future {
          Thread.sleep(300); actor ! "1"; println(1)
        }
        Future {
          Thread.sleep(200); actor ! "2"; println(2)
        }
        Future {
          Thread.sleep(100); actor ! "3"; println(3)
        }*/
      })

    val x: Source[String, _] = Source(List("kiki", "koo", "bar"))


    Ok.chunked(s via Comet.string("parent.cometMessage")).as(ContentTypes.XML)

    //def jsonSource: Source[JsValue, _] = Source(List(JsString("jsonString")))
    //Ok.chunked(jsonSource via Comet.text("parent.cometMessage"))

  }


}
