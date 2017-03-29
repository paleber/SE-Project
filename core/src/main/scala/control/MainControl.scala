package control

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.Timeout
import control.MainControl.RegisterView
import model.general.{DefaultActor, IdGenerator}
import model.msg.ClientMsg.LoadLevel
import model.msg.{ClientMsg, ServerMsg}
import persistence.LevelManager
import scaldi.Module

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

object MainControl {

  case class RegisterView(view: ActorRef)

  def props: Props = Props[MainControl]

}

private class MainControl extends Actor with ActorLogging {

  private implicit val timeout: Timeout = 5.seconds

  private val levelManager = context.actorOf(LevelManager.props, "levelManager")

  private val views = ListBuffer.empty[ActorRef]
  private var subControl = context.actorOf(Props[DefaultActor], "init")

  override def receive: Receive = {
    case RegisterView(view) =>
      views += view

    case ClientMsg.LoadMenu =>
      log.debug("Showing Menu")

      context.stop(subControl)

      subControl = context.actorOf(Props[DefaultActor], s"menu-${IdGenerator.generate()}")
      self ! ServerMsg.MenuLoaded

    case ClientMsg.LoadLevel(id) =>
      log.debug("Showing Game")

      levelManager ! LoadLevel(id)



     /*
      val level = LevelManager.load(levelName)
      if (level.isDefined) {
        log.info(s"Start Game: $levelName")
        context.stop(subControl)
        subControl = context.actorOf(Props(new GameControl(level.get)), s"game-${IdGenerator.generate()}")

        (subControl ? InternalMsg.GetGame).mapTo[Level].map { game => // TODO previous it was mapped to game
          views.foreach(_ ! ServerMsg.ShowLevel(game))
        }

      } else {
        log.error(s"Level $levelName is unknown")
      } */

    case msg: ClientMsg =>
      subControl.forward(msg)

    case msg: ServerMsg =>
      views.foreach(_ ! msg)

  }

  override def postStop: Unit = {
    views.foreach(_ ! PoisonPill)
  }

}
