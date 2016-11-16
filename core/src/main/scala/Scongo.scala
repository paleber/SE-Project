import akka.actor.{ActorSystem, Props}
import control.MainControl
import gui.Gui
import model.msg.ClientMsg.ShowMenu
import tui.Tui

object Scongo extends App {

  private val system = ActorSystem("scongo")

  private val main = system.actorOf(MainControl.props(Map(
    "tui" -> Props[Tui],
    "gui" -> Props[Gui])
  ), "main")

  main ! ShowMenu

}
