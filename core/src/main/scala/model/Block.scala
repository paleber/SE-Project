package model

import engine.Point

case class Block(grids: Array[Grid], // First half: frontSide, second half: backSide
                 curGrid: Int,
                 position: Point)
