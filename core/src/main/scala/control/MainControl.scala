package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import model.general.{DefaultActor, IdGenerator}
import model.loader.LevelLoader
import model.msg.{ClientMessage, ServerMessage}

class MainControl extends Actor with ActorLogging {

  var subControl: ActorRef = context.actorOf(Props[DefaultActor], "init")

  var views = List.empty[ActorRef]


  override def receive = {

    case ClientMessage.ShowMenu =>
      log.debug("Showing Menu")

      context.stop(subControl)

      subControl = context.actorOf(Props[DefaultActor], s"menu-${IdGenerator.generate()}")
      self ! ServerMessage.ShowMenu

    case ClientMessage.ShowGame(levelName) =>
      log.debug("Showing Game")

      val level = LevelLoader.load(levelName)
      if (level.isDefined) {
        log.info(s"Start Game: $levelName")
        context.stop(subControl)
        subControl = context.actorOf(Props(new GameControl(level.get)), s"game-${IdGenerator.generate()}")

        import akka.pattern.ask
        import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global

        implicit val timeout: Timeout = 5.seconds


        (subControl ? Init).mapTo[InitGame].map { msg =>
          self ! ServerMessage.ShowGame(msg)
        }


      } else {
        log.error(s"Level $levelName is unknown")
      }

    case ClientMessage.RegisterView(view) =>
      log.debug("Registering view: " + context.sender.path)
      views = view :: views

    case ClientMessage.Shutdown =>
      log.info("Shutdown")
      context.system.terminate
      System.exit(1)

    case msg: ClientMessage =>
      subControl.forward(msg)

    case msg: ServerMessage =>
      views.foreach(view => view.forward(msg))

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

}
