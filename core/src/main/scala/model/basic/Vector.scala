package model.basic

object Vector {

  def stretch(from: Point, to: Point): Vector = {
    Vector(to.x - from.x, to.y - from.y)
  }

}

case class Vector(x: Double, y: Double) {

  def rotate(angle: Double): Vector = {
    var p = Point(x, y)
    p = p.rotate(angle)
    Vector(p.x, p.y)
  }

  def *(factor: Double): Vector = {
    Vector(x * factor, y * factor)
  }

  def angle = {
    (Math.atan2(-y, x) + 2 * Math.PI) % Math.PI
  }

}
