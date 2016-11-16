package tui

import java.io.{BufferedReader, InputStreamReader}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import control.MainControl
import model.console.{ConsoleInput, ConsoleOutput, TextCmdParser}
import model.msg.{ClientMsg, ServerMsg}

object Tui {
  def props(control: ActorRef) = Props(new Tui(control))
}

private class Tui(control: ActorRef) extends Actor with ActorLogging {
  log.debug("Initializing")

  control ! MainControl.RegisterView(self)

  private val parser = context.actorOf(Props[TextCmdParser], "parser")

  private val readConsoleThread = new Thread(new Runnable {
    override def run() = {
      val reader = new BufferedReader(new InputStreamReader(System.in))
      try {
        while (!Thread.currentThread.isInterrupted) {
          if (reader.ready) {
            parser ! ConsoleInput(reader.readLine)
          } else {
            Thread.sleep(10)
          }
        }
      } catch {
        case e: InterruptedException =>
        case e: NoSuchElementException =>
      } finally {
        reader.close()
      }
    }
  })
  readConsoleThread.start()

  override def receive = {

    case ClientMsg.Shutdown =>
      control ! PoisonPill

    case msg: ClientMsg =>
      control ! msg

    case msg: ServerMsg =>
      log.info(msg.toString)

    case msg: ConsoleOutput =>
      log.info(msg.toString)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  override def postStop = {
    readConsoleThread.interrupt()
  }

}