import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{DefaultOptimalSizeExploringResizer, SmallestMailboxPool}
import control.MainControl
import control.MainControl.CreateAndRegisterView
import gui.Gui
import persistence.ResourceManager.LoadMenu
import persistence.{FilePersistence, Persistence, ResourceManager}
import scaldi.Module
import scaldi.akka.AkkaInjectable
import tui.Tui

object Scongo extends App with AkkaInjectable {

  private implicit val system = ActorSystem("scongo")
  private implicit val injector = ScongoModule


  private val main = injectActorRef[MainControl]("main")

  main ! CreateAndRegisterView(injectActorProps[Tui], "tui")
  main ! CreateAndRegisterView(injectActorProps[Gui], "gui")

  main ! LoadMenu

}

object ScongoModule extends Module with AkkaInjectable {

  binding to ActorSystem("scongo")
  binding toProvider new MainControl

  binding toProvider new Tui
  binding toProvider new Gui

  bind[Persistence] toProvider new FilePersistence

  bind[ActorRef] identifiedBy 'resourceRouter to {

    inject[ActorSystem].actorOf(
      SmallestMailboxPool(1)
        .withResizer(DefaultOptimalSizeExploringResizer())
        .props(Props(new ResourceManager())),
      "resourceRouter"
    )
  }

}
