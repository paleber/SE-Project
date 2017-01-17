package models

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import control.MainControl
import model.console.CmdParser
import model.msg.{ClientMsg, ParserMsg, ScongoMsg, ServerMsg}
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization

object Wui {

  def props(control: ActorRef, socket: ActorRef) = Props(new Wui(control, socket))

}

class Wui(control: ActorRef, connection: ActorRef) extends Actor with ActorLogging {
  log.info("Initializing")

  control ! MainControl.RegisterView(self)

  private val parser = context.actorOf(Props[CmdParser], "parser")

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ScongoMsg])))

  override def receive: PartialFunction[Any, Unit] = {

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
    control ! PoisonPill
  }

}
