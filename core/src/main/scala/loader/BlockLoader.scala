package loader

import engine.Point
import model.{Block, BlockGrids, Grid}


object BlockLoader {

  def load(grid: Grid, rotationSteps: Int): Block = {

    Block(
      Array(
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array()),
        BlockGrids(grid, Array(), Array(), Array(), Array())
      ), 0, Point(0, 0)
    )
  }

}
