package module

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.DefaultOptimalSizeExploringResizer
import akka.routing.SmallestMailboxPool
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import control.UserControl
import gui.Gui
import persistence.FilePersistence
import persistence.Persistence
import persistence.ResourceManager
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import scaldi.Module
import tui.Tui

object ScongoModule extends Module {

  binding to ActorSystem("scongo")
  binding toProvider new UserControl

  binding toProvider new Tui
  binding toProvider new Gui

  bind[Persistence] to new FilePersistence("core/src/main/resources/levels")

  bind[ActorRef] identifiedBy 'resourceRouter to {
    inject[ActorSystem].actorOf(
      SmallestMailboxPool(1)
        .withResizer(DefaultOptimalSizeExploringResizer())
        .props(Props(new ResourceManager())),
      "resourceRouter"
    )
  }

  bind[Materializer] to ActorMaterializer()(inject[ActorSystem])
  bind[WSClient] to AhcWSClient()(inject[Materializer])

}
