package control

import akka.actor.{ActorSystem, Props}


object Starter extends App {

  private val system = ActorSystem()

  private val mainReceiver = system.actorOf(Props[MainReceiver], "mainReceiver")

  system.actorOf(Props(new Tui(mainReceiver)), "tui")
  system.actorOf(Props(new Gui(mainReceiver)), "gui")

}
