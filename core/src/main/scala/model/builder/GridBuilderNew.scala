package model.builder

import model.basic._
import model.element.{AnchoredGrid, Grid}
import persistence.ResourceLoader.NewLevelPlan

import scala.collection.mutable
import scala.collection.mutable.ListBuffer





object GridBuilderNew {

  case class NewGrid(anchors: List[Point], polygons: List[List[Point]], edges: List[Line])

  case class ConstructedLevel(board: NewGrid,
                              blocks: List[NewGrid],
                              width: Double,
                              height: Double)

  private case class Element(anchor: Point, lines: List[Line])

  private val (anchorToAnchors: Map[Int, Array[Vector]], anchorToCorners: Map[Int, Array[Vector]]) = {

    def createLines(form: Int) = {
      var p = Point.ORIGIN
      var v = Vector(0, 1)
      val lines = new Array[Line](form)

      for (i <- 0 until form) {
        val q = p + v
        lines(i) = Line(p, q)
        p = q
        v = v.rotate(Math.PI * 2 / form)
      }

      val centeringVector = Vector.stretch(lines(lines.length / 2 - 1).end, Point.ORIGIN) * 0.5
      lines.transform(l => l + centeringVector)
    }

    val lines = Map(4 -> createLines(4), 6 -> createLines(6))

    def createAnchorVectors(form: Int): Array[Vector] = {
      lines(form).map(line => Vector.stretch(Point.ORIGIN, line.mid) * 2).toArray
    }

    def createCornerVectors(form: Int): Array[Vector] = {
      lines(form).map(line => Vector.stretch(Point.ORIGIN, line.start)).toArray
    }

    (Map(4 -> createAnchorVectors(4),
      6 -> createAnchorVectors(6)),
      Map(4 -> createCornerVectors(4),
        6 -> createCornerVectors(6)))

  }

  def build(plan: NewLevelPlan): ConstructedLevel = {
    val anchorVectors = anchorToAnchors(plan.form)
    val cornerVectors = anchorToCorners(plan.form)

    def shiftAnchors(shifts: List[Int]): Point = {
      Point.ORIGIN + shifts.map(s => anchorVectors(s)).sum
    }

    def createBlockAnchors(blockShifts: List[List[Int]]): List[Point] = {
      blockShifts.map(s => shiftAnchors(s))
    }

    val blockAnchors = plan.shifts.map(s => createBlockAnchors(s))
    val boardAnchors = blockAnchors.flatten

    def createBlock(anchors: List[Point]): NewGrid = {




       NewGrid(anchors, null, null)
    }


    // anchors / List[List[PolygonPoint]] / List[BorderLines]
    val boardElements: List[Element] = null
    val blockElements: List[List[Element]] = null

    val anchors = buildAnchors(dirs, plan.shifts)
    centerElements(coreLines, anchors)

    val allLines = buildAllLines(coreLines, anchors)
    val lines = extractInnerLines(allLines)
    optimizeLines(lines)
    optimizeLines(allLines)

    val corners = buildCorners(allLines)
    AnchoredGrid(Grid(corners, lines.toList), plan.form, anchors.toList)
  }

  private def centerElements(coreLines: Array[Line], anchors: Array[Point]) = {
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
    coreLines.transform(l => l + v)
    anchors.transform(a => a + v)
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

  private def buildAllLines(coreLines: Array[Line], anchors: Array[Point]): ListBuffer[Line] = {
    val lines = ListBuffer.empty[Line]
    anchors.foreach(anchor => {
      val v = Vector.stretch(anchors(0), anchor)
      coreLines.foreach(l => lines += l + v)
    })
    lines
  }

  private def extractInnerLines(allLines: ListBuffer[Line]): ListBuffer[Line] = {
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
    innerLines
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
      if (allLines(i).start.distanceTo(p) < 1e-5 || allLines(i).end.distanceTo(p) < 1e-5) {
        return allLines.remove(i)
      }
    }
    throw new IllegalStateException("Next line not found, inconsistent plan")
  }

  private def optimizeLines(lines: ListBuffer[Line]): Unit = {
    for (i <- lines.indices) {
      for (j <- (i + 1) until lines.length) {
        val line = lines(i).connect(lines(j), 1e-5)
        if (line.isDefined) {
          lines.remove(j)
          lines.remove(i)
          lines += line.get
          optimizeLines(lines)
          return
        }
      }
    }
  }

}
