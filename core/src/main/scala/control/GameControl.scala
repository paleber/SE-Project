package control

import loader.LevelLoader
import model.{Game, Level}


object GameControl {

  def createGame(levelId: Level): Game = {
    new Game(LevelLoader.load)
  }

}
