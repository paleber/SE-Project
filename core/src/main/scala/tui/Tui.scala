package tui

import java.util.Scanner

import akka.actor.{Actor, ActorLogging, Props}
import model.console.{ConsoleInput, ConsoleOutput, TextCmdParser}
import model.msg.{ClientMsg, ErrorMsg, ServerMsg}


class Tui extends Actor with ActorLogging {
  log.debug("Initializing")

  private val main = context.actorSelection("../control")
  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  new Thread(new Runnable {
    override def run(): Unit = {
      val scanner = new Scanner(System.in)
      while (true) {
        try {
          parser ! ConsoleInput(scanner.nextLine)
        } catch {
          case e: NoSuchElementException => return
        }
      }
    }
  }).start()

  override def receive = {

    case msg: ClientMsg =>
      main ! msg

    case msg: ServerMsg =>
      log.info(msg.toString)

    case msg: ConsoleOutput =>
      log.info(msg.toString)

    case ErrorMsg(msg) =>
      log.error(msg)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

}