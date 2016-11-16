package control

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import control.MainControl.RegisterView
import model.element.Game
import model.general.{DefaultActor, IdGenerator}
import model.msg.{ClientMsg, InternalMsg, ServerMsg}
import persistence.LevelManager

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object MainControl {

  case class RegisterView(view: ActorRef)

  def props = Props[MainControl]

}

private class MainControl extends Actor with ActorLogging {

  private implicit val timeout: Timeout = 5.seconds

  private val views = ListBuffer.empty[ActorRef]
  private var subControl = context.actorOf(Props[DefaultActor], "init")

  override def receive = {
    case RegisterView(view) =>
      views += view

    case ClientMsg.ShowMenu =>
      log.debug("Showing Menu")

      context.stop(subControl)

      subControl = context.actorOf(Props[DefaultActor], s"menu-${IdGenerator.generate()}")
      self ! ServerMsg.ShowMenu

    case ClientMsg.ShowGame(levelName) =>
      log.debug("Showing Game")

      val level = LevelManager.load(levelName)
      if (level.isDefined) {
        log.info(s"Start Game: $levelName")
        context.stop(subControl)
        subControl = context.actorOf(Props(new GameControl(level.get)), s"game-${IdGenerator.generate()}")

        (subControl ? InternalMsg.GetGame).mapTo[Game].map { game =>
          views.foreach(view => view ! ServerMsg.ShowGame(game))
        }

      } else {
        log.error(s"Level $levelName is unknown")
      }

    case msg: ClientMsg =>
      subControl.forward(msg)

    case msg: ServerMsg =>
      views.foreach(view => view.forward(msg))

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  override def postStop = {
    views.foreach(_ ! PoisonPill)
  }

}
