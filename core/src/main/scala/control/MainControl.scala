package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import loader.LevelLoader
import model.plan.LevelPlan
import msg.{ClientMessage, ServerMessage}
import util.{DefaultActor, IdGenerator}


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
        self ! ServerMessage.ShowGame(levelName, level.get)
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
      log.debug("Forwarding ClientMessage to SubControl")
      subControl.forward(msg)


    case msg: ServerMessage =>
      log.debug("Forwarding ServerMessage to Views: " + msg)
      views.foreach(view => view.forward(msg))


    case msg =>
      log.warning("Unhandled message: " + msg)

  }

}
