import akka.actor.ActorSystem
import control.MainControl
import control.MainControl.CreateAndRegisterView
import gui.Gui
import persistence.Persistence.LoadMenu
import persistence.{FilePersistence, Persistence}
import scaldi.Module
import scaldi.akka.AkkaInjectable
import tui.Tui

object Scongo extends App with AkkaInjectable{

  private implicit val system = ActorSystem("scongo")
  private implicit val injector  = ScongoModule

  private val main = injectActorRef[MainControl]("main")

  main ! CreateAndRegisterView(injectActorProps[Tui], "tui")
  main ! CreateAndRegisterView(injectActorProps[Gui], "gui")

  main ! LoadMenu

}

object ScongoModule extends Module{

  bind[MainControl] toProvider new MainControl

  bind[Tui] toProvider new Tui
  bind[Gui] toProvider new Gui

  bind[Persistence] toProvider new FilePersistence

}
