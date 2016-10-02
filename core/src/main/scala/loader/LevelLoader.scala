package loader

import model.{Block, Level, Point}

object LevelLoader {

  val rotationSteps = 4

  def load(levelIndex: Int): Option[Level] = {

    if (levelIndex != 0) {
      return None
    }

    Some(Level(9, 7, GridLoader.load(3) + Point(4.5, 2),
      List(
        Block(GridLoader.load(0), Point(2.5, 4.5)),
        Block(GridLoader.load(1), Point(4.5, 4.5)),
        Block(GridLoader.load(2), Point(7, 4.5))
      ), 4
    ))

  }
}
