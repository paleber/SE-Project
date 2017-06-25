package builder

import model.basic._
import model.element.{Grid, Level, LevelKey, Plan}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object LevelBuilder {

  private[builder] val (anchorVectors: Map[Int, List[Vector]], cornerVectors: Map[Int, List[Vector]]) = {

    println("InitBuilder")

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
      lines.transform(l => l + centeringVector).toList
    }

    val lines = Map(4 -> createLines(4), 6 -> createLines(6))

    def createAnchorVectors(form: Int): List[Vector] = {
      lines(form).map(line => Vector.stretch(Point.ZERO, line.mid) * 2)
    }

    def createCornerVectors(form: Int): List[Vector] = {
      lines(form).map(line => Vector.stretch(Point.ZERO, line.start))
    }

    (Map(4 -> createAnchorVectors(4),
      6 -> createAnchorVectors(6)),
      Map(4 -> createCornerVectors(4),
        6 -> createCornerVectors(6)))

  }

  def build(id: LevelKey, plan: Plan): Level = {

    val aVectors = anchorVectors(plan.form)
    val cVectors = cornerVectors(plan.form)

    def shiftAnchors(shifts: List[Int]): Point = {
      shifts.map(s => aVectors(s)).foldLeft(Point.ZERO)(_ + _)
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

    def createGrid(anchors: List[Point], isBoard: Boolean): Grid = {

      val corners = anchors.map(a =>
        cVectors.map(v => a + v))

      val edges = corners.flatMap(corners =>
        corners.indices.map(index =>
          Line(corners(index), corners((index + 1) % corners.length)))).to[ListBuffer]

      // remove redundant edges
      for (l <- edges; k <- edges if k != l && k.equalsNearly(l, 1e-5)) {
        edges -= l
        if (!isBoard) {
          edges -= k
        }
      }

      // connect neighbor edges with same direction
      for (l <- edges; k <- edges if l != k) {
        val c = l.connect(k, 1e-5)
        if (c.isDefined && edges.contains(l)) {
          edges -= l
          edges -= k
          edges += c.get
        }
      }

      // TODO optimize polygons

      Grid(anchors, corners, edges.toList, Point.ZERO)
    }

    val decentralizedBlockAnchors = plan.shifts.map(createAnchors)
    val board = createGrid(centerAnchors(decentralizedBlockAnchors.flatten), isBoard = true)
    val blocks = decentralizedBlockAnchors.map(a => createGrid(centerAnchors(a), isBoard = false))

    def findOptimalSize(level: Level): Level = {
      val field = AnchorField(level.form, level.size)

      Try(
        Range(0, 20).foreach(_ =>
          new Game(level, field)
        )
      ) match {
        case Success(_) =>
          val toleranceSize = level.size + 3
          val field = AnchorField(level.form, toleranceSize)
          level.copy(size = toleranceSize, width = field.width, height = field.height)

        case Failure(_) => findOptimalSize(level.copy(size = level.size + 1))
      }
    }

    findOptimalSize(Level(
      id = id,
      form = plan.form,
      size = 0,
      width = 0,
      height = 0,
      board = board,
      blocks = blocks)
    )

  }


}
