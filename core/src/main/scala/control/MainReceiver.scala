package control

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}

// Receives Event from View
class MainReceiver extends Actor {

  /*
   new LevelControl
   new GameControl

   // empfängt Events, UserEvents an Controller weiter
   // leitet ServerEvents an alle Views weiter

   */

println(context.sender)

  var views: List[View] = List.empty[View]

  // var activeControl

  val x = context.actorOf(Props[MainSender])
  // x ! "Test"

  context.stop(x)

  override def receive = {

    case RegisterView(view) => views = view :: views

    case ShowMenu =>
      println("TODO: ShowMenu")

    case ShowGame =>
      println("TODO: ShowGame")

    case Shutdown =>
      context.stop(self)
      println("TODO: Shutdown")

    case _ =>
      println("TODO: Redirect to current Controller")

  }

}

// Sends Event to UI
class MainSender extends Actor {

  override def receive = {
    case _ => println("MainSender: " + context.sender)
      context.sender ! "Test"
  }

  override def postStop(): Unit = super.postStop()


}


object Starter extends App {

  val system = ActorSystem("HelloSystem")

  val main = system.actorOf(Props[MainReceiver])

  main ! Shutdown
  // create MainControl Actor
  // create TuiActor, let ist register by MainControl actor
  // same with gui
  // val helloActor = system.actorOf(Props[HelloActor], name = "helloactor")

}



case class RegisterView(view: View)

case object ShowMenu

case object ShowGame

case object Shutdown