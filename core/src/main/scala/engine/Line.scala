package engine


case class Line(start: Point, end: Point) {

  def +(p: Point): Line = {
    Line(start + p, end + p)
  }

  def rotate(angle: Double, pivot: Point = Point.ORIGIN): Line = {
    Line(start.rotate(angle, pivot), end.rotate(angle, pivot))
  }

  def mirrorVertical(percentage: Double = 1): Line = {
    Line(start.mirrorVertical(percentage), end.mirrorVertical(percentage))
  }

  def mirrorHorizontal(percentage: Double = 1): Line = {
    Line(start.mirrorHorizontal(percentage), end.mirrorHorizontal(percentage))
  }

}
