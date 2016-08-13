package loader

import engine.{Line, Point}
import model.Grid


object GridLoader {

  def loadGrid1: Grid = {

    val a = Point(-0.5, -1)
    val b = Point(0.5, -1)
    val c = Point(0.5, 1)
    val d = Point(-0.5, 1)

    Grid(
      Array(
        Point(0, -0.5),
        Point(0, 0.5)
      ),
      Array(
        Line(a, b),
        Line(b, c),
        Line(c, d),
        Line(d, a)
      ),
      Array(
        Line(Point(-0.5, 0), Point(0.5, 0))
      )
    )
  }

}
