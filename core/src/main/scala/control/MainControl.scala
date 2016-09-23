package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import loader.LevelLoader
import msg.{ClientMessage, ServerMessage}
import util.{IdGenerator, InitActor}


class MainControl extends Actor with ActorLogging {

  var subControl: ActorRef = context.actorOf(Props[InitActor], "init")

  var views = List.empty[ActorRef]


  override def receive = {

    case ClientMessage.ShowMenu =>
      log.debug("Showing Menu")

      context.stop(subControl)

      subControl = context.actorOf(Props[MenuControl], s"menu-${IdGenerator.generate()}")
      self ! ServerMessage.ShowMenu

    case ClientMessage.ShowGame(levelIndex) =>
      log.debug("Showing Game")
      val level = LevelLoader.load(levelIndex)
      if (level.isDefined) {
        log.info(s"Start Game: ${level.get}")
        context.stop(subControl)
        subControl = context.actorOf(Props(new GameControl(level.get)), s"game-${IdGenerator.generate()}")
        self ! ServerMessage.ShowGame(level.get)
      } else {
        log.error(s"Level $levelIndex is unknown")
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
      log.error("Unhandled message: " + msg)

  }

}
