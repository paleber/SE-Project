package control

import akka.actor.{Actor, ActorLogging, ActorRef}


class Tui(mainReceiver: ActorRef) extends Actor with ActorLogging {

  log.debug("Initialize Tui")
  mainReceiver ! RegisterView(self)

  override def receive = {
    case msg => println("TODO: " + msg)
  }

}
