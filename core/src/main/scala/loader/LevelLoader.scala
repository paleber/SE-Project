package loader

import engine.Point
import model.{Block, Level}

object LevelLoader {

  def load: Level = {

    Level(9, 7, GridLoader.load(3), Point(4.5, 2),
      Array(
        Block(BlockCreator.create(GridLoader.load(0)), 0, Point(2.5, 4.5)),
        Block(BlockCreator.create(GridLoader.load(1)), 0, Point(4.5, 4.5)),
        Block(BlockCreator.create(GridLoader.load(2)), 0, Point(7, 4.5))
      )
    )
  }

}
