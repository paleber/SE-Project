package loader

import engine.Grid

import scala.collection.mutable

object BlockCreator {



  def create(grid: Grid): List[Grid] = {
    val grids = mutable.ListBuffer.empty[Grid]

    for (i <- 0 to grid.rotationSteps) {
      grids += grid.rotate(i * (Math.PI / 2))
    }

    val mirroredGrid = grid.mirrorHorizontal()
    for (i <- 0 to grid.rotationSteps) {
      grids += mirroredGrid.rotate(i * (Math.PI / 2))
    }

    grids.toList
  }



}
