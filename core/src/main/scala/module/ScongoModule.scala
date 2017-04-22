package module

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{DefaultOptimalSizeExploringResizer, SmallestMailboxPool}
import control.UserControl
import gui.Gui
import persistence.ResourceManager
import scaldi.Module
import tui.Tui

object ScongoModule extends Module  {

  binding to ActorSystem("scongo")
  binding toProvider new UserControl

  binding toProvider new Tui
  binding toProvider new Gui

  bind[ActorRef] identifiedBy 'resourceRouter to {
    inject[ActorSystem].actorOf(
      SmallestMailboxPool(1)
        .withResizer(DefaultOptimalSizeExploringResizer())
        .props(Props(new ResourceManager())),
      "resourceRouter"
    )
  }

}
