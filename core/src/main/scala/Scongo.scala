import akka.actor.ActorSystem
import control.UserControl
import control.UserControl.CreateAndRegisterView
import gui.Gui
import module.ScongoModule
import persistence.FilePersistenceModule
import persistence.ResourceManager.LoadMenu
import scaldi.akka.AkkaInjectable
import tui.Tui

object Scongo extends App with AkkaInjectable {

  private implicit val injector = ScongoModule ++
    new FilePersistenceModule("core/src/main/resources/levels")

  private implicit val system = inject[ActorSystem]

  private val main = injectActorRef[UserControl]("main")

  main ! CreateAndRegisterView(injectActorProps[Tui], "tui")
  main ! CreateAndRegisterView(injectActorProps[Gui], "gui")

  main ! LoadMenu

}
