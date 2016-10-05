package tui

import java.util.Scanner

import akka.actor.{Actor, ActorLogging, Props}
import msg.{ClientMessage, ServerMessage}


class Tui extends Actor with ActorLogging {
  log.debug("Initializing")

  private val main = context.actorSelection("../control")
  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  new Thread(new Runnable {
    override def run(): Unit = {
      val scanner = new Scanner(System.in)
      while (true) {
        parser ! ConsoleInput(scanner.nextLine)
      }
    }
  }).start()

  override def receive = {

    case msg: ClientMessage =>
      main ! msg

    case msg: ServerMessage =>
      log.info(msg.toString)

    case msg: ConsoleOutput =>
      log.info(msg.toString)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

}