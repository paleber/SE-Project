package loader

import engine.Point
import model.{Block, Level}

object LevelLoader {

  val rotationSteps = 4

  def load(levelIndex: Int): Option[Level] = {

    if (levelIndex != 0) {
      return None
    }

    Some(Level(9, 7, GridLoader.load(3) + Point(4.5, 2),
      List(
        Block(BlockCreator.create(GridLoader.load(0)), 0, Point(2.5, 4.5)),
        Block(BlockCreator.create(GridLoader.load(1)), 0, Point(4.5, 4.5)),
        Block(BlockCreator.create(GridLoader.load(2)), 0, Point(7, 4.5))
      )
    ))

  }
}
