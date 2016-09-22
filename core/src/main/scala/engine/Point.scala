package engine

object Point {
  val ORIGIN = Point(0, 0)
}

case class Point(x: Double, y: Double) {

  def +(p: Point): Point = {
    Point(x + p.x, y + p.y)
  }

  def rotate(angle: Double, pivot: Point = Point.ORIGIN): Point = {
    val sin = Math.sin(angle)
    val cos = Math.cos(angle)

    val xn = x - pivot.x
    val yn = y - pivot.y

    Point(
      xn * cos - yn * sin + pivot.x,
      xn * sin + yn * cos + pivot.y
    )
  }

  def mirrorYAxis(): Point = {
    Point(-x, y)
  }

}
