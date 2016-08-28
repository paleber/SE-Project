package control

import akka.actor.{Actor, ActorLogging, ActorRef}
import loader.LevelLoader
import model.{Game, Level}


class GameControl(mainSender: ActorRef) extends Actor with ActorLogging {
  log.debug("initializing")

  def createGame(levelId: Level): Game = {
    new Game(LevelLoader.load)
  }

  override def receive = {
    case _ => log.warning("TODO")
  }



}
