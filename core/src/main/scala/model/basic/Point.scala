package model.basic

import scala.math.BigDecimal.RoundingMode

object Point {
  val ZERO = Point(0, 0)
}

case class Point(x: Double, y: Double) {

  def +(p: Point): Point = {
    Point(x + p.x, y + p.y)
  }

  def +(v: Vector): Point = {
    Point(x + v.x, y + v.y)
  }

  def distanceTo(p: Point): Double = {
    Math.sqrt(distanceSquareTo(p))
  }

  def distanceSquareTo(p: Point): Double = {
    (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y)
  }

  def rotate(angle: Double, pivot: Point = Point.ZERO): Point = {
    val sin = Math.sin(angle)
    val cos = Math.cos(angle)

    val xn = x - pivot.x
    val yn = y - pivot.y

    Point(
      xn * cos - yn * sin + pivot.x,
      xn * sin + yn * cos + pivot.y
    )
  }

  def mirrorVertical(percentage: Double = 1): Point = {
    Point(x - 2 * x * percentage, y)
  }

  def mirrorHorizontal(percentage: Double = 1): Point = {
    Point(x, y - 2 * y * percentage)
  }

  override def toString: String = {
    val xs = BigDecimal(x).setScale(4, RoundingMode.HALF_UP).toDouble
    val ys = BigDecimal(y).setScale(4, RoundingMode.HALF_UP).toDouble
    s"Point($xs,$ys)"
  }

}
