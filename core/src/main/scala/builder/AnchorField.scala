package builder

import model.basic.Point

import scala.collection.mutable.ListBuffer


object AnchorField {

  def apply(form: Int, size: Int): AnchorField = anchorField(form)(size)

  private val maxNeighborDistance: Map[Int, Double] = Map(
    4 -> 1.43,
    6 -> 1.75
  )

  private val anchorVectors = LevelBuilder.anchorVectors.map { case (n, v) => (n, v.map(_ * 0.5)) }

  private val anchorField: Map[Int, Stream[AnchorField]] = Map(
    4 -> Stream.from(3).map(n => build(4, n)),
    6 -> Stream.from(3).map(n => build(6, n))
  )

  private def build(form: Int, n: Int): AnchorField = {

    val distance = if (form == 4) {
      maxNeighborDistance(form) * 0.6
    } else {
      maxNeighborDistance(form) * 0.7
    }

    val aVectors = anchorVectors(form)

    val width = (aVectors.head.x * n + distance) * 2
    val height = width * 0.625

    val border = distance
    val xyMin = border - 0.1
    val xMax = width - border + 0.1
    val yMax = height - border + 0.1

    val anchors = ListBuffer.empty[Point]

    def addAnchor(p: Point): Unit = {
      if (p.x > xyMin &&
        p.x < xMax &&
        p.y > xyMin &&
        p.y < yMax &&
        !anchors.exists(_.distanceSquareTo(p) < 0.1)
      ) {
        anchors += p
        aVectors.foreach(v => addAnchor(p + v))
      }
    }

    addAnchor(Point(width / 2, height / 2))
    AnchorField(anchors.toList, width, height, maxNeighborDistance(form))
  }

}

case class AnchorField(anchors: List[Point], width: Double, height: Double, maxNeighborDistance: Double) {

  val neighbors: Map[Point, List[Point]] = {
    val maxDistanceSquare = maxNeighborDistance * maxNeighborDistance
    anchors.map(a => (a, anchors.filter(_.distanceSquareTo(a) < maxDistanceSquare))).toMap
  }

}
