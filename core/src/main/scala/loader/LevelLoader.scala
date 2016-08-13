package loader

import engine.Point
import model.Level

object LevelLoader {

  def load: Level = {

    Level(15, 10, GridLoader.loadGrid1, Point(7.5, 5), null)

  }

}
