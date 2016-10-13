import akka.actor.{ActorSystem, Props}
import control.MainControl
import gui.Gui
import model.msg.ClientMsg.ShowMenu
import model.msg.ClientMsg.RegisterView
import tui.Tui

object Scongo extends App {

  private val system = ActorSystem("scongo")
  private val main = system.actorOf(Props[MainControl], "control")

  main ! RegisterView(system.actorOf(Props[Tui], "tui"))
  main ! RegisterView(system.actorOf(Props[Gui], "gui"))

  main ! ShowMenu

}
