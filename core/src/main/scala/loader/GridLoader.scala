package loader

import model.{Grid, Line, Point, Vector}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


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


  // 4
  // 1
  // 2
  // 2 2
  val plan3x2 = GridPlan(4, List(
    List(3),
    List(0, 3),
    List(0) /*,
    List(0, 1),
    List(1)*/
  ))


  def centerElements(coreLines: Array[Line], anchors: Array[Point]) = {
    var xMin = anchors(0).x
    var xMax = anchors(0).x
    var yMin = anchors(0).y
    var yMax = anchors(0).y
    for (i <- 1 until anchors.length) {
      xMin = Math.min(xMin, anchors(i).x)
      xMax = Math.max(xMax, anchors(i).x)
      yMin = Math.min(yMin, anchors(i).y)
      yMax = Math.max(yMax, anchors(i).y)
    }

    val v = Vector.stretch(Point((xMin + xMax) / 2, (yMin + yMax) / 2), Point.ORIGIN)
    println(v)
    println(coreLines.toList)
    coreLines.transform(l => l + v)

    anchors.transform(a => a + v)
  }


  def buildGrid(plan: GridPlan): GridWithAnchors = {
    val coreLines = buildCoreLines(plan.rotationSteps)
    val dirs = buildDirections(coreLines)
    val anchors = buildAnchors(dirs, plan.shifts)

    centerElements(coreLines, anchors)
    println(anchors.toList)

    val allLines = buildAllLines(coreLines, anchors)

    println("---")
    allLines.foreach(f => println(f))
    println("---")

    println(allLines.length)
    val lines = extractInnerLines(allLines)

    // TODO optimize allLines and lines



    val corners = buildCorners(allLines)

    println(corners)


    null // TODO
  }

  private def buildCoreLines(rotationSteps: Int): Array[Line] = {
    var p = Point.ORIGIN
    var v = Vector(1, 0)
    val lines = new Array[Line](rotationSteps)

    for (i <- 0 until rotationSteps) {
      val q = p + v
      lines(i) = Line(p, q)
      p = q
      v = v.rotate(Math.PI * 2 / rotationSteps)
    }

    v = Vector.stretch(lines(lines.length / 2 - 1).end, Point.ORIGIN) * 0.5
    for (i <- 0 until rotationSteps) {
      lines(i) = lines(i) + v
    }

    lines
  }

  private def buildDirections(coreLines: Array[Line]): List[Vector] = {
    val dirs = new Array[Vector](coreLines.length)
    for (i <- coreLines.indices) {
      dirs(i) = Vector.stretch(Point.ORIGIN, coreLines(i).mid) * 2
    }
    dirs.toList
  }

  private def buildAnchors(dirs: List[Vector], shifts: List[List[Int]]): Array[Point] = {
    val anchors = mutable.ListBuffer(Point.ORIGIN)
    shifts.foreach(shift => {
      var p = Point.ORIGIN
      shift.foreach(shiftIndex => p += dirs(shiftIndex))
      anchors += p
    })
    anchors.toArray
  }

  def buildAllLines(coreLines: Array[Line], anchors: Array[Point]): ListBuffer[Line] = {
    val lines = ListBuffer.empty[Line]
    anchors.foreach(anchor => {
      val v = Vector.stretch(Point.ORIGIN, anchor)
      coreLines.foreach(l => lines += l + v)
    })
    lines
  }

  def extractInnerLines(allLines: ListBuffer[Line]): List[Line] = {
    val innerLines = ListBuffer.empty[Line]

    var index = 0
    while (index < allLines.length) {

      val line = extractEqualLine(allLines, index)
      if (line.isDefined) {
        innerLines += line.get
      } else {
        index += 1
      }
    }

    innerLines.toList
  }

  private def extractEqualLine(lines: ListBuffer[Line], startIndex: Int): Option[Line] = {
    for (i <- (startIndex + 1) until lines.length) {
      if (lines(startIndex).equalsNearly(lines(i), 1e-5)) {
        lines.remove(i)
        return Some(lines.remove(startIndex))
      }
    }
    None
  }

  private def buildCorners(allLines: ListBuffer[Line]): List[Point] = {
    var line = allLines.remove(0)
    var corners = ListBuffer(line.start)

    while (allLines.nonEmpty) {
      if (line.start.distanceTo(corners.last) > line.end.distanceTo(corners.last)) {
        corners += line.start
      } else {
        corners += line.end
      }
      line = extractNextLine(allLines, corners.last)
    }

    corners.toList
  }

  private def extractNextLine(allLines: ListBuffer[Line], p: Point): Line = {
    for (i <- allLines.indices) {
      if (allLines(i).start.distanceTo(p) < 1e5 || allLines(i).end.distanceTo(p) < 1e5) {
        return allLines.remove(i)
      }
    }
    throw new IllegalStateException("Next line not found, should never happen")
  }


}

case object Test extends App {
  GridLoader.buildGrid(GridLoader.plan3x2)
}

case class GridPlan(rotationSteps: Int, shifts: List[List[Int]])

case class GridWithAnchors(grid: Grid, anchors: List[Point])