package builder

import model.basic.Point

import scala.collection.mutable.ListBuffer


object AnchorBuilder {

  private val anchorDistanceMap: Map[Int, Double] = Map(
    4 -> 1.43,
    6 -> 1.75
  )

  private val anchorVectors = LevelBuilder.anchorVectors.map { case (n, v) => (n, v.map(_ * 0.5)) }

  val anchorField: Map[Int, Stream[AnchorField]] = Map(
    4 -> Stream.from(2).map(n => build(4, n)),
    6 -> Stream.from(2).map(n => build(6, n))
  )

  private def build(form: Int, n: Int): AnchorField = {

    val border = anchorDistanceMap(form)
    val aVectors = anchorVectors(form)

    val width = (aVectors.head.x * n + border) * 2
    val height = width * 0.625

    val xyMin = border - 0.1
    val xMax = width - border + 0.1
    val yMax = height - border + 0.1

    val anchors = ListBuffer.empty[Point]

    def addAnchor(p: Point): Unit = {
      if (p.x < xyMin || p.x > xMax) {
        return
      }
      if (p.y < xyMin || p.y > yMax) {
        return
      }
      for (a <- anchors if a.distanceSquareTo(p) < 0.1) {
        return
      }
      anchors += p
      aVectors.foreach(v => addAnchor(p + v))
    }

    addAnchor(Point(width / 2, height / 2))
    AnchorField(anchors.toList, width, height, border)
  }

}

case class AnchorField(anchors: List[Point], width: Double, height: Double, maxNeighborDistance: Double) {

  val neighbors: Map[Point, List[Point]] = {
    val maxDistanceSquare = maxNeighborDistance * maxNeighborDistance
    anchors.map(a => (a, anchors.filter(_.distanceSquareTo(a) < maxDistanceSquare))).toMap
  }

}
