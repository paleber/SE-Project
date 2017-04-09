package models

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import control.UserControl
import control.UserControl.{CreateAndRegisterView, Shutdown}
import gui.Gui
import model.console.CmdParser
import model.msg.{ClientMsg, ParserMsg, ScongoMsg, ServerMsg}
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization
import persistence.ResourceManager.LoadMenu
import scaldi.Injector
import scaldi.akka.AkkaInjectable

object Wui {

  def props(socket: ActorRef, isProductive: Boolean)(implicit injector: Injector) = Props(new Wui(socket, isProductive))

}

class Wui(connection: ActorRef, isProductive: Boolean)(implicit inj: Injector) extends Actor with AkkaInjectable with ActorLogging {
  log.info("Initializing")

  private val control = injectActorRef[UserControl]("main")

  if (!isProductive) {
    control ! CreateAndRegisterView(injectActorProps[Gui], "gui")
  }

  control ! UserControl.RegisterView(self)
  control ! LoadMenu

  private val parser = context.actorOf(Props[CmdParser], "parser")

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ScongoMsg])))

  override def receive: Receive = {

    case msg: String =>
      parser ! msg

    case msg: ParserMsg =>
      connection ! Serialization.write(msg)

    case msg: ClientMsg =>
      control ! msg

    case msg: ServerMsg =>
      connection ! Serialization.write(msg)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  override def postStop: Unit = {
    log.debug("Stopping")
    control ! Shutdown
  }

}
