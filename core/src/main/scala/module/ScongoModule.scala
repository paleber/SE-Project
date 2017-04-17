package module

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{DefaultOptimalSizeExploringResizer, SmallestMailboxPool}
import control.UserControl
import gui.Gui
import persistence.{FilePersistence, MongoPersistence, Persistence, ResourceManager}
import scaldi.Module
import scaldi.akka.AkkaInjectable
import tui.Tui

object ScongoModule extends Module with AkkaInjectable {

  binding to ActorSystem("scongo")
  binding toProvider new UserControl

  binding toProvider new Tui
  binding toProvider new Gui

  //bind[Persistence] to new FilePersistence
  //bind[String] identifiedBy 'filePath to "core/src/main/resources/levels"

  bind[Persistence] to new MongoPersistence
  bind[String] identifiedBy 'mongoUri to "localhost:20004"
  bind[String] identifiedBy 'mongoDatabase to "scongo"


  bind[ActorRef] identifiedBy 'resourceRouter to {
    inject[ActorSystem].actorOf(
      SmallestMailboxPool(1)
        .withResizer(DefaultOptimalSizeExploringResizer())
        .props(Props(new ResourceManager())),
      "resourceRouter"
    )
  }

}
