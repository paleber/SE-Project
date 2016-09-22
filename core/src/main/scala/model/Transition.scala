package model

/** Map the current State of a Block to the next State. */
case class Transition(rightRotation: Int,
                      leftRotation: Int,
                      mirrorVertical: Int,
                      mirrorHorizontal: Int)

object Transition {

  /** Map rotationSteps to BlockTransition. */
  private val transitions: Map[Int, List[Transition]] = Map(
    (4, List(
      Transition(1, 3, 4, 6),
      Transition(2, 2, 7, 5),
      Transition(3, 1, 6, 4),
      Transition(0, 0, 5, 7),
      Transition(5, 7, 0, 2),
      Transition(6, 6, 3, 1),
      Transition(7, 5, 2, 0),
      Transition(4, 4, 1, 3)
    )),
    (6, List(// TODO
      Transition(1, 3, 4, 6),
      Transition(2, 2, 7, 5),
      Transition(3, 1, 6, 4),
      Transition(0, 0, 5, 7),
      Transition(5, 7, 0, 2),
      Transition(6, 6, 3, 1),
      Transition(7, 5, 2, 0),
      Transition(4, 4, 1, 3),
      Transition(8, 8, 8, 8),
      Transition(9, 9, 9, 9),
      Transition(10, 10, 10, 10),
      Transition(11, 11, 11, 11)
    ))
  )

  def transitionMap(rotationSteps: Int): List[Transition] = {
    val transition = transitions.get(rotationSteps)
    if (transition.isEmpty) {
      throw new IllegalArgumentException()
    }
    transition.get
  }

}
