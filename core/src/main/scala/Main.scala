import akka.actor.{ActorSystem, Props}
import control.MainControl
import gui.Gui
import tui.Tui

object Main extends App {

  private val system = ActorSystem()

  system.actorOf(Props[MainControl], "control")
  system.actorOf(Props[Tui], "tui")
  system.actorOf(Props[Gui], "gui")

}
