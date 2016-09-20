package model

import engine.Point

case class Block(grids: List[Grid],
                 position: Point,
                 state: Int)
