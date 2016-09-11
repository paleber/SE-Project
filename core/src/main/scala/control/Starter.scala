package control

import akka.actor.{ActorSystem, Props}
import tui.Tui

object Starter extends App {

  private val system = ActorSystem()

  system.actorOf(Props[MainControl], "control")
  system.actorOf(Props[Tui], "tui")
  system.actorOf(Props[Gui], "gui")

}
