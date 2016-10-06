package controllers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import control.MainControl
import gui.Gui
import msg.ClientMessage.RegisterView
import msg.{ClientMessage, ServerMessage}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import tui.{ConsoleInput, ConsoleOutput, TextCmdParser, Tui}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

case object WuiConsole {

  case object GetMessages

  case class Messages(messages: List[String])

}

class WuiConsole extends Actor with ActorLogging {

  private val main = context.actorSelection("../control")
  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  private val msgBuffer = ListBuffer.empty[String]

  override def receive = {
    case msg: ConsoleInput => parser ! msg
    case msg: ConsoleOutput => msgBuffer += msg.toString
    case msg: ClientMessage => main ! msg
    case msg: ServerMessage => msgBuffer += msg.toString
    case WuiConsole.GetMessages => sender ! WuiConsole.Messages(msgBuffer.toList)
    case msg => log.warning("Unhandled message: " + msg)
  }

}


class Application extends Controller {

  implicit val timeout: Timeout = 5.seconds

  private val system = ActorSystem()
  private val main = system.actorOf(Props[MainControl], "control")

  main ! RegisterView(system.actorOf(Props[Tui], "tui"))
  main ! RegisterView(system.actorOf(Props[Gui], "gui"))

  private val wuiConsole = system.actorOf(Props[WuiConsole], "console")
  main ! RegisterView(wuiConsole)

  main ! ClientMessage.ShowMenu


  def index = Action {
    Ok("Hello world")
  }

  def hello(name: String) = Action {
    Ok("Hello " + name)
  }




  def console = Action.async {
    (wuiConsole ? WuiConsole.GetMessages).mapTo[WuiConsole.Messages].map { msgList =>
      Ok(views.html.hello(msgList.messages))
    }
  }


}

