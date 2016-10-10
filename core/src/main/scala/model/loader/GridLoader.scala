package model.loader

import java.io.File

import model.element.{GridExtended, Grid, GridPlan}
import model.basic
import model.basic.{Line, Point}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source


object GridLoader {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private case class MapItem(plan: GridPlan,
                             grid: Option[GridExtended] = None)

  private val map = {
    val dirGrids = new File("core/src/main/resources/grids")
    val map = mutable.Map.empty[String, MapItem]
    for (file <- dirGrids.listFiles()) {
      val source = Source.fromFile(file).mkString
      val plan = read[GridPlan](source)
      val name = file.getName.replace(".json", "")
      map.update(name, MapItem(plan))
    }
    map
  }

  def load(name: String): GridExtended = {
    assert(map.contains(name))
    val item = map(name)
    if (item.grid.isDefined) {
      return item.grid.get
    }
    val grid = buildGrid(item.plan)
    map.update(name, item.copy(grid = Some(grid)))
    grid
  }

  private[loader] def buildGrid(plan: GridPlan): GridExtended = {
    val coreLines = buildCoreLines(plan.form)
    val dirs = buildDirections(plan.form)
    val anchors = buildAnchors(dirs, plan.shifts)
    centerElements(coreLines, anchors)

    val allLines = buildAllLines(coreLines, anchors)
    val lines = extractInnerLines(allLines)
    optimizeLines(lines)
    optimizeLines(allLines)

    val corners = buildCorners(allLines)
    GridExtended(Grid(corners, lines.toList), plan.form, anchors.toList)
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

    val v = basic.Vector.stretch(Point((xMin + xMax) / 2, (yMin + yMax) / 2), Point.ORIGIN)
    coreLines.transform(l => l + v)
    anchors.transform(a => a + v)
  }

  private def buildCoreLines(rotationSteps: Int): Array[Line] = {
    var p = Point.ORIGIN
    var v = basic.Vector(0, 1)
    val lines = new Array[Line](rotationSteps)

    for (i <- 0 until rotationSteps) {
      val q = p + v
      lines(i) = Line(p, q)
      p = q
      v = v.rotate(Math.PI * 2 / rotationSteps)
    }

    v = basic.Vector.stretch(lines(lines.length / 2 - 1).end, Point.ORIGIN) * 0.5
    for (i <- 0 until rotationSteps) {
      lines(i) = lines(i) + v
    }

    lines
  }

  private def buildAnchors(dirs: List[basic.Vector], shifts: List[List[Int]]): Array[Point] = {
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
      val v = basic.Vector.stretch(anchors(0), anchor)
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

  def buildDirections(rotationSteps: Int): List[basic.Vector] = {
    val lines = buildCoreLines(rotationSteps)
    val dirs = ListBuffer.empty[basic.Vector]
    lines.foreach(l =>
      dirs += basic.Vector.stretch(Point.ORIGIN, l.mid) * 2
    )
    dirs.toList
  }

}
