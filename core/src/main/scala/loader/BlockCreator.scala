package loader

import model.{Transition, Grid}

object BlockCreator {

  /** Map rotationSteps to BlockTransition. */
  val transitions: Map[Int, Transition] = Map((
    4, Transition(
      Array(1, 2, 3, 0, 5, 6, 7, 4),
      Array(3, 2, 1, 0, 7, 6, 5, 4),
      Array(4, 7, 6, 5, 0, 3, 2, 1),
      Array(6, 5, 4, 7, 2, 1, 0, 3)
    ))
  )

  // TODO real implementation
  def create(grid: Grid, rotationSteps: Int): Array[Grid] = {
    Array(
      grid, grid, grid, grid,
      grid, grid, grid, grid
    )
  }

  def getBlockTransition(rotationSteps: Int): Transition = {
    transitions(rotationSteps)
    // TODO log error-message when not found (NoSuchElementException is actually thrown)
  }

}


