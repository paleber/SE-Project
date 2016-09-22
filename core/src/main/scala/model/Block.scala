package model

import engine.{Grid, Point}

case class Block(grids: List[Grid],
                 position: Point,
                 state: Int)
