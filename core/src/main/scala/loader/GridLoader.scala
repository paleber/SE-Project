package loader

import engine.{Line, Point}
import model.Grid


object GridLoader {

  def loadGrid1: Grid = {

    Grid(
      Array(
        Point(0, -0.5),
        Point(0, 0.5)
      ),
      Array(
        Point(-0.5, -1),
        Point(0.5, -1),
        Point(0.5, 1),
        Point(-0.5, 1)
      ),
      Array(
        Line(Point(-0.5, 0), Point(0.5, 0))
      )
    )
  }

}
