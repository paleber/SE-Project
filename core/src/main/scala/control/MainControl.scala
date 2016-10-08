package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import model.general.{DefaultActor, IdGenerator}
import model.loader.LevelLoader
import model.msg.{ClientMessage, InternalMessage, ServerMessage}
import akka.pattern.ask
import model.element.Level
import model.msg.ServerMessage.ShowGame

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class MainControl extends Actor with ActorLogging {

  private implicit val timeout: Timeout = 5.seconds

  private var subControl: ActorRef = context.actorOf(Props[DefaultActor], "init")

  private var views = ListBuffer.empty[ActorRef]

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

        (subControl ? InternalMessage.GetGame).mapTo[Level].map { game =>
          views.foreach(view => view ! ShowGame(game))
        }


      } else {
        log.error(s"Level $levelName is unknown")
      }

    case ClientMessage.RegisterView(view) =>
      log.debug("Registering view: " + context.sender.path)
      views += view

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
