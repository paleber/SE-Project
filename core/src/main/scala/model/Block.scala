package model

import engine.{Grid, Point}

case class Block(grids: List[Grid],
                 gridIndex: Int,
                 position: Point) {

  def activeGrid: Grid = {
    grids(gridIndex)
  }

}