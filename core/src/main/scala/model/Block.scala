package model

import engine.Point

case class Block(grids: Array[Grid],
                 position: Point,
                 state: Int)
