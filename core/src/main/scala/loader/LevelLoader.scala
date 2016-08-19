package loader

import engine.Point
import model.Level

object LevelLoader {

  def load: Level = {

    Level(15, 10, GridLoader.load(3), Point(7.5, 5), null)

  }

}
