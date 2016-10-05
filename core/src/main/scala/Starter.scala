import akka.actor.{ActorSystem, Props}
import control.MainControl
import gui.Gui
import msg.ClientMessage
import msg.ClientMessage.RegisterView
import tui.Tui

object Starter extends App {
  private val system = ActorSystem()
  private val main = system.actorOf(Props[MainControl], "control")
  main ! RegisterView(system.actorOf(Props[Tui], "tui"))
  main ! RegisterView(system.actorOf(Props[Gui], "gui"))
  main ! ClientMessage.ShowMenu
}
