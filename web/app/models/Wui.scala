package models

import akka.actor.{Actor, ActorLogging, Props}
import model.console.{ConsoleInput, ConsoleOutput, TextCmdParser}
import model.msg.ServerMsg.{ShowGame, ShowMenu, UpdateBlock}
import model.msg.{ClientMsg, ScongoMsg, ServerMsg}

import scala.collection.mutable.ListBuffer

case object Wui {

  case object ReadMsgBuffer

  case class MsgBuffer(messages: List[ScongoMsg])

}

class Wui extends Actor with ActorLogging {

  private val main = context.actorSelection("../control")
  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  private val msgBuffer = ListBuffer.empty[ScongoMsg]

  override def receive = {

    case msg: ConsoleInput =>
      parser ! msg

    case msg: ClientMsg =>
      main ! msg

    case msg: ConsoleOutput =>
      msgBuffer += msg

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
      sender ! Wui.MsgBuffer(msgBuffer.toList)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

}
