package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import loader.LevelLoader
import msg.{ClientMessage, ServerMessage}


class MainControl extends Actor with ActorLogging {

  var activeControl = context.actorOf(Props[MenuControl], "menu")
  var views = List.empty[ActorRef]

  override def receive = {

    case ClientMessage.ShowMenu =>
      log.debug("Switching to Menu")
      context.stop(activeControl)
      activeControl = context.actorOf(Props[MenuControl], "menu")

    case ClientMessage.ShowGame(levelIndex) =>
      log.debug("Switching to Game")
      showGame(levelIndex)

    case ClientMessage.RegisterView(view) =>
      log.debug("Registering view")
      views = view :: views

    case ClientMessage.Shutdown =>
      log.info("Shutdown")
      context.system.terminate
      System.exit(1)

    case msg: ClientMessage =>
      log.debug("Forwarding ClientMessage")
      activeControl.forward(msg)

    case msg: ServerMessage =>
      log.debug("Forwarding ServerMessage")
      views.foreach(view => view.forward(msg))

    case _ =>
      log.error("Unknown message")

  }

  private def showGame(levelIndex: Int): Unit = {
    val level = LevelLoader.load(levelIndex)
    if (level.isDefined) {
      log.info(s"Start Game with Level $level")
      context.stop(activeControl)
      context.actorOf(Props(new GameControl(level.get)), "game")
    } else {
      log.error(s"Level $level is unknown")
    }
  }

}
