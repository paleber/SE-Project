package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/** Receives all events from views, handle them or forward them to the active control. */
class MainReceiver extends Actor with ActorLogging {

  val mainSender = context.actorOf(Props[MainSender], "mainSender")

  val menuControl = context.actorOf(Props(new MenuControl(mainSender)))
  val gameControl = context.actorOf(Props(new GameControl(mainSender)))

  var activeControl: ActorRef = menuControl


  override def receive = {

    case msg: RegisterView =>
      mainSender ! msg

    case ShowMenu =>
      println("TODO: ShowMenu")

    case ShowGame =>
      println("TODO: ShowGame")

    case Shutdown =>
      context.stop(self)
      println("TODO: Shutdown")

    case _ =>
      activeControl.forward _
      println("TODO: Redirect to current Controller")

  }

}

// Sends Event to UI
class MainSender extends Actor with ActorLogging {

  var views = List.empty[ActorRef]

  override def receive = {

    case RegisterView(view) =>
      log.debug("Register view")
      views = view :: views

    case _ => println("MainSender: " + context.sender)
      context.sender ! "Test"

  }

  override def postStop = {
    for (v <- views) {
      context.stop(v)
    }
  }

}
