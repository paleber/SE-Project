package loader

import model.{Grid, Line, Point}

object GridLoader {

  // TODO load from files or database
  def load(gridId: Int): Grid = {

    gridId match {

      case 0 =>
        Grid(
          4,
          List(
            Point(0, 0)
          ),
          List(
            Point(-0.5, -0.5),
            Point(0.5, -0.5),
            Point(0.5, 0.5),
            Point(-0.5, 0.5)
          ),
          List.empty
        )

      case 1 =>
        Grid(
          4,
          List(
            Point(0, -0.5),
            Point(0, 0.5)
          ),
          List(
            Point(-0.5, -1),
            Point(0.5, -1),
            Point(0.5, 1),
            Point(-0.5, 1)
          ),
          List(
            Line(Point(-0.5, 0), Point(0.5, 0))
          )
        )

      case 2 =>
        Grid(
          4,
          List(
            Point(-0.5, -0.5),
            Point(-0.5, 0.5),
            Point(0.5, -0.5)
          ),
          List(
            Point(-1, -1),
            Point(1, -1),
            Point(1, 0),
            Point(0, 0),
            Point(0, 1),
            Point(-1, 1)
          ),
          List(
            Line(Point(-1, 0), Point(0, 0)),
            Line(Point(0, -1), Point(0, 0))
          )
        )

      case 3 =>
        Grid(
          4,
          List(
            Point(-1, -0.5),
            Point(0, -0.5),
            Point(1, -0.5),
            Point(-1, 0.5),
            Point(0, 0.5),
            Point(1, 0.5)
          ),
          List(
            Point(-1.5, -1),
            Point(1.5, -1),
            Point(1.5, 1),
            Point(-1.5, 1)
          ),
          List(
            Line(Point(-1.5, 0), Point(1.5, 0)),
            Line(Point(-0.5, -1), Point(-0.5, 1)),
            Line(Point(0.5, -1), Point(0.5, 1))
          )
        )

      case _ => throw new IllegalArgumentException

    }
  }

}
