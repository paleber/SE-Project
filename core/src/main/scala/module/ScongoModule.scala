package module

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{DefaultOptimalSizeExploringResizer, SmallestMailboxPool}
import akka.stream.{ActorMaterializer, Materializer}
import control.UserControl
import gui.Gui
import persistence.ResourceManager
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import scaldi.Module
import tui.Tui

import scala.concurrent.ExecutionContext.Implicits._

object ScongoModule extends Module  {

  binding to ActorSystem("scongo")
  binding toProvider new UserControl

  binding toProvider new Tui
  binding toProvider new Gui

 /* bind[ActorRef] identifiedBy 'resourceRouter to {
    inject[ActorSystem].actorOf(
      SmallestMailboxPool(1)
        .withResizer(DefaultOptimalSizeExploringResizer())
        .props(Props(new ResourceManager())),
      "resourceRouter"
    )
  }*/

  println("----------")




  bind[Materializer] to ActorMaterializer()(inject[ActorSystem])



  bind[WSClient] to AhcWSClient()(inject[Materializer])

  /*private implicit val system = inject[ActorSystem]
  val x = ActorMaterializer()
  println(x.isShutdown)
  bind[Materializer] to ActorMaterializer()

  private implicit val materializer = ActorMaterializer()


  bind[WSClient] to AhcWSClient()*/


}
