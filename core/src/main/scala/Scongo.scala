import akka.actor.{Actor, ActorSystem, Props}
import control.MainControl
import gui.Gui
import model.general.DefaultActor
import model.msg.ClientMsg.LoadMenu
import scaldi.akka.AkkaInjectable
import scaldi.{Injector, Module}
import tui.Tui

object Scongo extends App {

  private implicit val inj: Injector = new ScongoModule

  private val system = ActorSystem("scongo")

  private val main = system.actorOf(MainControl.props, "main")

  system.actorOf(Tui.props(main), "tui")
  system.actorOf(Gui.props(main), "gui")

  main ! LoadMenu

}

class ScongoModule extends Module{
  bind[Actor] identifiedBy 'abc to new DefaultActor
}

class T extends AkkaInjectable {

  val x = injectActorProps[DefaultActor] (identified by 'abc)
  val y = injectActorRef[DefaultActor]

}