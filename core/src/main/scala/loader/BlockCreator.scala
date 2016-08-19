package loader

import model.Grid

object BlockCreator {

  // TODO real implementation
  def create(grid: Grid): Array[Grid] = {
    Array(
      grid, grid, grid, grid,
      grid, grid, grid, grid
    )
  }

}
