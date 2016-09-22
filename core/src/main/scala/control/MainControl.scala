package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import loader.LevelLoader
import msg.{ClientMessage, ServerMessage}


class MainControl extends Actor with ActorLogging {

  var subControl: Option[ActorRef] = None

  var views = List.empty[ActorRef]

  var idCounter = 0

  def generateId: String = {
    val id = idCounter.toString
    idCounter += 1
    id
  }

  override def receive = {

    case ClientMessage.ShowMenu =>
      log.debug("Showing Menu")
      if (subControl.isDefined) {
        context.stop(subControl.get)
      }
      subControl = Option(context.actorOf(Props[MenuControl], s"menu-$generateId"))
      self ! ServerMessage.ShowMenu

    case ClientMessage.ShowGame(levelIndex) =>
      log.debug("Showing Game")
      val level = LevelLoader.load(levelIndex)
      if (level.isDefined) {
        log.info(s"Start Game: ${level.get}")
        if (subControl.isDefined) {
          context.stop(subControl.get)
        }
        subControl = Option(context.actorOf(Props(new GameControl(level.get)), s"game-$generateId"))
        self ! ServerMessage.ShowGame(level.get)
      } else {
        log.error(s"Level $level is unknown")
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
      if (subControl.isDefined) {
        subControl.get.forward(msg)
      } else {
        log.error("MainControl is initializing, no SubControl active")
      }

    case msg: ServerMessage =>
      log.debug("Forwarding ServerMessage to Views: " + msg)
      views.foreach(view => view.forward(msg))



    case msg =>
      log.error("Unhandled message: " + msg)

  }

}
