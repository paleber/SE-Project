package model.builder

import model.basic._
import model.element.{Grid, Plan}

object GridBuilderNew {


  case class ConstructedLevel(board: Grid,
                              blocks: List[Grid],
                              width: Double,
                              height: Double)

  private val (anchorToAnchors: Map[Int, Array[Vector]], anchorToCorners: Map[Int, Array[Vector]]) = {

    def createLines(form: Int) = {
      var p = Point.ZERO
      var v = Vector(0, 1)
      val lines = new Array[Line](form)

      for (i <- 0 until form) {
        val q = p + v
        lines(i) = Line(p, q)
        p = q
        v = v.rotate(Math.PI * 2 / form)
      }

      val centeringVector = Vector.stretch(lines(lines.length / 2 - 1).end, Point.ZERO) * 0.5
      lines.transform(l => l + centeringVector)
    }

    val lines = Map(4 -> createLines(4), 6 -> createLines(6))

    def createAnchorVectors(form: Int): Array[Vector] = {
      lines(form).map(line => Vector.stretch(Point.ZERO, line.mid) * 2).toArray
    }

    def createCornerVectors(form: Int): Array[Vector] = {
      lines(form).map(line => Vector.stretch(Point.ZERO, line.start)).toArray
    }

    (Map(4 -> createAnchorVectors(4),
      6 -> createAnchorVectors(6)),
      Map(4 -> createCornerVectors(4),
        6 -> createCornerVectors(6)))

  }

  def build(plan: Plan): ConstructedLevel = {
    val anchorVectors = anchorToAnchors(plan.form)
    val cornerVectors = anchorToCorners(plan.form)

    def shiftAnchors(shifts: List[Int]): Point = {
      Point.ZERO + shifts.map(s => anchorVectors(s)).reduce(_ + _)
    }

    def createAnchors(blockShifts: List[List[Int]]): List[Point] = {
      blockShifts.map(s => shiftAnchors(s))
    }

    def centerAnchors(anchors: List[Point]): List[Point] = {
      val x = anchors.map(_.x)
      val y = anchors.map(_.y)
      val v = Vector(-0.5 * (x.min + x.max), -0.5 * (y.min + y.max))
      anchors.map(_ + v)
    }

    val blockAnchors = plan.shifts.map(s => centerAnchors(createAnchors(s)))
    val boardAnchors = centerAnchors(blockAnchors.flatten)

    def createGrid(anchors: List[Point]): Grid = {

      val corners = anchors.map(a =>
        cornerVectors.map(v => a + v).toList)

      val edges = corners.flatMap(corners =>
        corners.indices.map(index =>
          Line(corners(index), corners((index + 1) % corners.length))))

      // TODO remove double border edges
      // TODO optimize border edges
      // TODO optimize polygons

      // TODO calc mid of the board
      Grid(anchors, corners, edges, Point(10, 5))
    }

    val board = createGrid(boardAnchors)
    val blocks = blockAnchors.map(createGrid)

    /*val width = {
      val x = boardAnchors.map(_.x)
      2 * (x.max - x.min)
    }

    val height = {
      val y = boardAnchors.map(_.y)
      2 * (y.max - y.min)
    }*/

    // TODO height = 0.625 * width
    // TODO calc board size correctly

    ConstructedLevel(board, blocks, 20, 15)

    /*val anchors = buildAnchors(dirs, plan.shifts)
    centerElements(coreLines, anchors)

    val allLines = buildAllLines(coreLines, anchors)
    val lines = extractInnerLines(allLines)
    optimizeLines(lines)
    optimizeLines(allLines)

    val corners = buildCorners(allLines)
    AnchoredGrid(Grid(corners, lines.toList), plan.form, anchors.toList) */
  }

  /*
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
  }*/

}
