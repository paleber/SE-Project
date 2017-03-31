package tui

import java.io.{BufferedReader, InputStreamReader}

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import model.console.CmdParser
import model.msg.{ClientMsg, ParserMsg, ServerMsg}
import scaldi.Injector
import scaldi.akka.AkkaInjectable
import tui.Tui.Shutdown


object Tui {

  case object Shutdown extends ClientMsg

}

final class Tui(implicit inj: Injector) extends Actor with AkkaInjectable with ActorLogging {
  log.debug("Initializing")

  //context.parent ! MainControl.RegisterView(self)

  private val parser = context.actorOf(Props[CmdParser], "parser")

  private val readConsoleThread = new Thread(new Runnable {
    override def run(): Unit = {
      val reader = new BufferedReader(new InputStreamReader(System.in))
      try {
        while (!Thread.currentThread.isInterrupted) {
          if (reader.ready) {
            parser ! reader.readLine
          } else {
            Thread.sleep(10)
          }
        }
      } catch {
        case _: InterruptedException =>
        case _: NoSuchElementException =>
      } finally {
        reader.close()
      }
    }
  })
  readConsoleThread.start()

  override def receive: Receive = {

    case Shutdown =>
      context.parent ! PoisonPill

    case msg: ClientMsg =>
      context.parent ! msg

    case msg: ServerMsg =>
      log.info(msg.toString)

    case msg: ParserMsg =>
      log.info(msg.toString)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  override def postStop: Unit = {
    readConsoleThread.interrupt()
  }

}