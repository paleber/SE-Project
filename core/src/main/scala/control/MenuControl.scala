package control

import akka.actor.{Actor, ActorLogging, ActorRef}

class MenuControl(mainSender: ActorRef) extends Actor with ActorLogging {
  log.debug("initializing")

  override def receive = {
    case _ => log.warning("TODO")
  }

  override def postStop = {
    println("postStop X")
  }

}
