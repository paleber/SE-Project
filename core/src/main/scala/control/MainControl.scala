package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import control.MainControl.{CreateAndRegisterView, RegisterView}
import model.msg.{ClientMsg, InternalMsg, ServerMsg}
import persistence.ResourceManager.{LevelLoaded, LoadLevel, LoadMenu, MenuLoaded}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

import scala.concurrent.duration._

object MainControl {

  // Create and add a view as child
  case class CreateAndRegisterView(props: Props, name: String) extends InternalMsg

  // Add an external view actor
  case class RegisterView(view: ActorRef) extends InternalMsg

}

class MainControl(implicit inj: Injector) extends Actor with AkkaInjectable with ActorLogging {
  log.debug("Initializing")

  private implicit val timeout: Timeout = 5.seconds

  private val levelManager = inject[ActorRef]('resourceRouter)
  private val game = context.actorOf(Props[GameControl], "game")

  private def receiveLoadMenu(views: List[ActorRef]): Receive = {
    case LoadMenu =>
      levelManager ! LoadMenu

    case msg: MenuLoaded =>
      log.debug("Switching state to menu")
      context.become(menuState(views))
      views.foreach(_ ! msg)
  }

  private def initState(views: List[ActorRef]): Receive =
    receiveLoadMenu(views).orElse {

      case RegisterView(view) =>
        log.debug("Registering view: " + view.path.name)
        context.become(initState(view :: views))

      case CreateAndRegisterView(props, name) =>
        log.debug("Creating view: " + name)
        val view = context.actorOf(props, name)
        context.become(initState(view :: views))

    }

  private def menuState(views: List[ActorRef]): Receive = {

    case msg: LoadLevel =>
      levelManager ! msg

    case msg: LevelLoaded =>
      log.debug("Switching state to game")
      context.become(gameState(views))
      views.foreach(_ ! msg)

  }

  private def gameState(views: List[ActorRef]): Receive =
    receiveLoadMenu(views) orElse {

      case msg: ServerMsg =>
        views.foreach(_ ! msg)

      case msg: ClientMsg =>
        game ! msg

    }

  override def receive: Receive = initState(List.empty)

  override def postStop: Unit = {
    log.debug("Stopping")
  }

}
