package control

import java.lang.IllegalStateException
import java.nio.BufferOverflowException
import java.util.{NoSuchElementException, Scanner}

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.Consumer
import msg.ClientMessage.RegisterView
import msg.{ClientMessage, ServerMessage}

class Tui extends Actor with ActorLogging {
  log.debug("Initialize")

  val mainControl = context.actorSelection("../control")
  mainControl ! RegisterView(self)

  val scanner = new Scanner(System.in)

  new Thread(new Runnable {
    override def run(): Unit = { while (true) {
      val line = scanner.nextLine()
      context.self ! line
    }}
  }).start()

  override def receive = {
    case "exit" => mainControl ! ClientMessage.Shutdown
    case msg: String => log.info("Readed: "  + msg)
    case ServerMessage.ShowGame(level) => log.warning("TODO: " + level)
    case msg => log.warning("TODO: " + msg)
  }

  override def postStop() = {
    scanner.close()
  }

}


