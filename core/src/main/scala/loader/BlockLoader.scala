package loader

import model.{Block, BlockState, Grid}


object BlockLoader {

  def load(grid: Grid, rotationSteps: Int): Block = {

    Block(
      Array(
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array()),
        BlockState(grid, Array(), Array(), Array(), Array())
      ), 0, 0, 0
    )
  }

}
