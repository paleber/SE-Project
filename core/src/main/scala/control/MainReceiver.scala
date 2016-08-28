package control

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/** Receives all events from views, handle them or forward them to the active control. */
class MainReceiver extends Actor with ActorLogging {

  val mainSender = context.actorOf(Props[MainSender], "mainSender")

  var activeControl = context.actorOf(Props(new MenuControl(mainSender)))
  var activeState: AppState = AppState.Menu

  override def receive = {

    case msg: RegisterView =>
      mainSender ! msg

    case ChangeState(state) =>
      log.debug("changing state")
      if (state != activeState) {
        activeState = state
        context.stop(activeControl)

        activeControl = state match {
          case AppState.Menu =>
            context.actorOf(Props(new MenuControl(mainSender)))
          case AppState.Game =>
            context.actorOf(Props(new GameControl(mainSender)))
          case _ =>
            log.error("unknown state")
            throw new IllegalArgumentException
        }

      } else {
        log.error("state already active")
      }


    case Shutdown =>
      log.info("shutdown")
      context.system.terminate

    case _ =>
      activeControl.forward _
      println("TODO: Redirect to current Controller")

  }

}

/** Sends Events to all views. */
class MainSender extends Actor with ActorLogging {

  var views = List.empty[ActorRef]

  override def receive = {

    case RegisterView(view) =>
      log.debug("Register view")
      views = view :: views

    case _ =>
      println("MainSender: " + context.sender)
      context.sender ! "Test"

  }

  override def postStop = {
    for (v <- views) {
      context.stop(v)
    }
  }

}
