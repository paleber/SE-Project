package engine


object Vector {

  def stretch(from: Point, to: Point): Vector = {
    Vector(to.x - from.x, to.y - from.y)
  }

}

case class Vector(x: Double, y: Double)