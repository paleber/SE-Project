package engine


case class Line(start: Point, end: Point) {

  def +(p: Point): Line = {
    Line(start + p, end + p)
  }

  def rotate(angle: Double, pivot: Point = Point.ORIGIN): Line = {
    Line(start.rotate(angle, pivot), end.rotate(angle, pivot))
  }

  def mirrorYAxis(): Line = {
    Line(start.mirrorYAxis(), end.mirrorYAxis())
  }

}
