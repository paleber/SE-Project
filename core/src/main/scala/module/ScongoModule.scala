package module

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{DefaultOptimalSizeExploringResizer, SmallestMailboxPool}
import control.MainControl
import gui.Gui
import persistence.{FilePersistence, Persistence, ResourceManager}
import scaldi.Module
import scaldi.akka.AkkaInjectable
import tui.Tui

object ScongoModule extends Module with AkkaInjectable {

  binding to ActorSystem("scongo")
  binding toProvider new MainControl

  binding toProvider new Tui
  binding toProvider new Gui

  bind[Persistence] to new FilePersistence
  bind[String] identifiedBy 'filePath to "core/src/main/resources/lvNew"

  bind[ActorRef] identifiedBy 'resourceRouter to {
    inject[ActorSystem].actorOf(
      SmallestMailboxPool(1)
        .withResizer(DefaultOptimalSizeExploringResizer())
        .props(Props(new ResourceManager())),
      "resourceRouter"
    )
  }

}
