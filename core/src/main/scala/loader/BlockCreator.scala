package loader

import model.{Transition, Grid}

object BlockCreator {

  /** Map rotationSteps to BlockTransition. */
  val transitions: Map[Int, List[Transition]] = Map((
    4, List(
      Transition(1, 3, 4, 6),
      Transition(2, 2, 7, 5),
      Transition(3, 1, 6, 4),
      Transition(0, 0, 5, 7),
      Transition(5, 7, 0, 2),
      Transition(6, 6, 3, 1),
      Transition(7, 5, 2, 0),
      Transition(4, 4, 1, 3)
    )
  ))

  // TODO real implementation
  def create(grid: Grid, rotationSteps: Int): List[Grid] = {
    List(
      grid, grid, grid, grid,
      grid, grid, grid, grid
    )
  }

  def getBlockTransition(rotationSteps: Int): List[Transition] = {
    transitions(rotationSteps)
    // TODO log error-message when not found (NoSuchElementException is actually thrown)
  }

}


