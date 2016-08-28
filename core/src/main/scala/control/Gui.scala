package control

import akka.actor.{Actor, ActorLogging, ActorRef}


class Gui(mainReceiver: ActorRef) extends Actor with ActorLogging {

  log.debug("Initialize Gui")
  mainReceiver ! RegisterView(self)

  override def receive = {
    case msg => println("TODO: " + msg)
  }

}
