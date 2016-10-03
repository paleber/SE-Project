package model

case class Line(start: Point, end: Point) {

  def equalsNearly(line: Line, tolerance: Double): Boolean = {
    start.distanceSquareTo(line.start) < tolerance &&
      end.distanceSquareTo(line.end) < tolerance ||
      start.distanceSquareTo(line.end) < tolerance &&
        end.distanceSquareTo(line.start) < tolerance
  }


  def +(p: Point): Line = {
    Line(start + p, end + p)
  }

  def +(v: Vector): Line = {
    Line(start + v, end + v)
  }

  def mid: Point = {
    Point((start.x + end.x) / 2, (start.y + end.y) / 2)
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
