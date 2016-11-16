import akka.actor.{ActorSystem, Props}
import control.MainControl
import gui.Gui
import model.msg.ClientMsg.ShowMenu
import tui.Tui

object Scongo extends App {

  private val system = ActorSystem("scongo")

  private val main = system.actorOf(MainControl.props, "main")

  system.actorOf(Tui.props(main), "tui")
  system.actorOf(Gui.props(main), "gui")

  main ! ShowMenu

}
