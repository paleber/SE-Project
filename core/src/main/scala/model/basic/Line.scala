package model.basic

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

  def connect(l: Line, tolerance: Double): Option[Line] = {
    val dir1 = Vector.stretch(start, end).angle
    val dir2 = Vector.stretch(l.start, l.end).angle

    if (Math.abs(dir1 - dir2) > tolerance) {
      return None
    }

    if (start.distanceSquareTo(l.start) < tolerance) {
      return Some(Line(end, l.end))
    }
    if (start.distanceSquareTo(l.end) < tolerance) {
      return Some(Line(end, l.start))
    }
    if (end.distanceSquareTo(l.start) < tolerance) {
      return Some(Line(start, l.end))
    }
    if (end.distanceSquareTo(l.end) < tolerance) {
      return Some(Line(start, l.start))
    }

    None
  }

  def mid: Point = {
    Point((start.x + end.x) / 2, (start.y + end.y) / 2)
  }

  def rotate(angle: Double, pivot: Point = Point.ZERO): Line = {
    Line(start.rotate(angle, pivot), end.rotate(angle, pivot))
  }

  def mirrorVertical(percentage: Double = 1): Line = {
    Line(start.mirrorVertical(percentage), end.mirrorVertical(percentage))
  }

  def mirrorHorizontal(percentage: Double = 1): Line = {
    Line(start.mirrorHorizontal(percentage), end.mirrorHorizontal(percentage))
  }

}
