package loader

import engine.{Line, Point}
import model.Grid

object GridLoader {

  // TODO load from files or database
  def load(id: Int): Grid = {

    id match {

      case 0 =>
        Grid(
          Array(
            Point(0, 0)
          ),
          Array(
            Point(-0.5, -0.5),
            Point(0.5, -0.5),
            Point(0.5, 0.5),
            Point(-0.5, 0.5)
          ),
          Array()
        )

      case 1 =>
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

      case 2 =>
        Grid(
          Array(
            Point(0, 0),
            Point(0, 1),
            Point(1, 0)
          ),
          Array(
            Point(-0.5, -0.5),
            Point(1.5, -0.5),
            Point(1.5, 0.5),
            Point(0.5, 0.5),
            Point(0.5, 1.5),
            Point(-0.5, 1.5)
          ),
          Array(
            Line(Point(-0.5, 0.5), Point(0.5, 0.5)),
            Line(Point(0.5, -0.5), Point(0.5, 0.5))
          )
        )

      case 3 =>
        Grid(
          Array(
            Point(-1, -0.5),
            Point(0, -0.5),
            Point(1, -0.5),
            Point(-1, 0.5),
            Point(0, 0.5),
            Point(1, 0.5)
          ),
          Array(
            Point(-1.5, -1),
            Point(1.5, -1),
            Point(1.5, 1),
            Point(-1.5, 1)
          ),
          Array(
            Line(Point(-1.5, 0), Point(1.5, 0)),
            Line(Point(-0.5, -1), Point(-0.5, 1)),
            Line(Point(0.5, -1), Point(0.5, 1))
          )
        )

      case _ => throw new IllegalArgumentException

    }
  }

}
