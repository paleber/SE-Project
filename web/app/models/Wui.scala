package models

import akka.actor.{Actor, ActorLogging, Props}
import msg.{ClientMessage, ServerMessage}
import tui.{ConsoleInput, ConsoleOutput, TextCmdParser}

import scala.collection.mutable.ListBuffer

case object Wui {

  case object ReadMsgBuffer

  case class MsgBuffer(messages: List[String])

}

class Wui extends Actor with ActorLogging {

  private val main = context.actorSelection("../control")
  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  private val msgBuffer = ListBuffer.empty[String]

  override def receive = {
    case msg: ConsoleInput => parser ! msg
    case msg: ConsoleOutput => msgBuffer += msg.toString
    case msg: ClientMessage => main ! msg
    case msg: ServerMessage => msgBuffer += msg.toString
    case Wui.ReadMsgBuffer => sender ! Wui.MsgBuffer(msgBuffer.toList)
    case msg => log.warning("Unhandled message: " + msg)
  }

}

