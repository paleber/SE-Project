import akka.actor.ActorSystem
import control.MainControl
import control.MainControl.CreateAndRegisterView
import gui.Gui
import module.ScongoModule
import persistence.ResourceManager.LoadMenu
import scaldi.akka.AkkaInjectable
import tui.Tui

object Scongo extends App with AkkaInjectable {

  private implicit val injector = ScongoModule
  private implicit val system = inject[ActorSystem]

  private val main = injectActorRef[MainControl]("main")

  main ! CreateAndRegisterView(injectActorProps[Tui], "tui")
  main ! CreateAndRegisterView(injectActorProps[Gui], "gui")

  main ! LoadMenu

}
