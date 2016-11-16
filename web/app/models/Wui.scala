package models

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import control.MainControl
import model.console.{ConsoleInput, ConsoleOutput, TextCmdParser}
import model.msg.{ClientMsg, ScongoMsg, ServerMsg}
import org.json4s.ShortTypeHints
import org.json4s.jackson.Serialization

object Wui {

  case object ReadMsgBuffer

  case class MsgBuffer(messages: List[ScongoMsg])

  def props(control: ActorRef, socket: ActorRef) = Props(new Wui(control, socket))

}

class Wui(control: ActorRef, socket: ActorRef) extends Actor with ActorLogging {
  log.info("Initializing")

  control ! MainControl.RegisterView(self)

  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ScongoMsg])))
  // private val msgBuffer = ListBuffer.empty[ScongoMsg]

  override def receive = {

    case msg: ClientMsg =>
      control ! msg

    case msg: ConsoleOutput =>
      socket ! Serialization.write(msg)
    //msgBuffer += msg

    case msg: ServerMsg =>
      socket ! Serialization.write(msg)

    case msg: String  =>
      parser ! ConsoleInput(msg)
    /*
  case ShowMenu =>
    msgBuffer.clear()
    msgBuffer += ShowMenu

  case msg: ShowGame =>
    msgBuffer.clear()
    msgBuffer += msg

  case msg: UpdateBlock =>
    msgBuffer.foreach {
      case oldMsg: UpdateBlock if oldMsg.index == msg.index =>
        msgBuffer -= oldMsg
      case _ =>
    }
    msgBuffer += msg

  case msg: ServerMsg =>
    msgBuffer += msg


  case Wui.ReadMsgBuffer =>
    sender ! Wui.MsgBuffer(msgBuffer.toList)*/

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  override def postStop = {
    control ! PoisonPill
  }

}
