package loader

import engine.Point
import model.{Block, Grid}


object BlockCreator {

  def create(grid: Grid, rotationSteps: Int, curGrid: Int, position: Point): Block = {

    Block(
      Array(
        grid, grid, grid, grid,
        grid, grid, grid, grid
      ), 0, Point(0, 0)
    )
  }

}
