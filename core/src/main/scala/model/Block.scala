package model




case class Block(states: Array[BlockState],
                 initialState: Int,
                 xPos: Double,
                 yPos: Double)


trait Rotation

object Rotation {
  case object Left extends Rotation
  case object Right
  case object Vertical
  case object Horizontal
}

case class BlockState(fixed: Grid,
                      transitionRight: Array[Grid],
                      transitionLeft: Array[Grid],
                      transitionVertical: Array[Grid],
                      transitionHorizontal: Array[Grid]
                     )



// enthält spwanpoint
// enthält Grids


// 4 Rotationsschritte
// 2 Seiten
// = 8 Stati

// später noch grids für zwischenschritte
