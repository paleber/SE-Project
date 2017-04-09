package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import builder.Game._
import builder.{AnchorField, Game}
import control.UserControl.{CreateAndRegisterView, RegisterView, Shutdown}
import model.element.{Grid, Level}
import model.msg.{ClientMsg, InternalMsg}
import persistence.ResourceManager._
import scaldi.Injector
import scaldi.akka.AkkaInjectable

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object UserControl {

  // Create and add a view as child
  case class CreateAndRegisterView(props: Props, name: String) extends InternalMsg

  // Add an external view actor
  case class RegisterView(view: ActorRef) extends InternalMsg

  case object Shutdown extends ClientMsg

}

class UserControl(implicit inj: Injector) extends Actor with AkkaInjectable with ActorLogging {
  log.debug("Initializing")

  private implicit val timeout: Timeout = 5.seconds

  private val resourceManager = inject[ActorRef]('resourceRouter)

  private def receiveLoadMenu(views: List[ActorRef]): Receive = {
    case LoadMenu =>
      Await.result(resourceManager ? LoadMenu, timeout.duration) match {
        case msg: MenuLoaded =>
          log.debug("Switching state to menu")
          context.become(menuState(views))
          views.foreach(_ ! msg)
      }
  }

  private def receiveShutdown(views: List[ActorRef]): Receive = {
    case Shutdown =>
      views.foreach(context.stop)
      context.stop(self)
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

  private def menuState(views: List[ActorRef]): Receive =
    receiveShutdown(views) orElse {

      case msg: LoadLevel =>
        Await.result(resourceManager ? msg, timeout.duration) match {

          case LevelLoaded(level) =>
            log.debug("Switching state to game")

            def initGame(level: Level): Unit = {
              Try(new Game(level, AnchorField(level.form, level.size))) match {
                case Success(game) =>
                  views.foreach(_ ! LevelLoaded(game.currentState))
                  context.become(gameState(views, game))
                case Failure(_) =>
                  val field = AnchorField(level.form, level.size + 1)
                  initGame(level.copy(
                    size = level.size + 1,
                    width = field.width,
                    height = field.height
                  ))
              }
            }

            initGame(level)

          case LoadingLevelFailed =>
            log.error("Loading level failed")

        }
    }

  private def gameState(views: List[ActorRef], game: Game): Receive =
    receiveLoadMenu(views) orElse
      receiveShutdown(views) orElse {

      case UpdateBlockPosition(index, position) =>
        updateGame(views, index, game.updateBlockPosition(index, position))

      case RotateBlockLeft(index) =>
        updateGame(views, index, game.rotateBlockLeft(index))

      case RotateBlockRight(index) =>
        updateGame(views, index, game.rotateBlockRight(index))

      case MirrorBlockVertical(index) =>
        updateGame(views, index, game.mirrorBlockVertical(index))

      case MirrorBlockHorizontal(index) =>
        updateGame(views, index, game.mirrorBlockHorizontal(index))

    }

  private def updateGame(views: List[ActorRef], index: Int, blockAndTime: (Grid, Option[Int])): Unit = {
    views.foreach(_ ! BlockUpdated(index, blockAndTime._1))
    if (blockAndTime._2.isDefined) {
      views.foreach(_ ! LevelFinished(blockAndTime._2.get))
    }
  }

  override def receive: Receive = initState(List.empty)

  override def postStop: Unit = {
    log.debug("Stopping")
  }

}
