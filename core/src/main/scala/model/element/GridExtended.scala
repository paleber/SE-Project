package model.element

import model.basic.Point

case class GridExtended(grid: Grid,
                        form: Int,
                        anchors: List[Point]) {

  def +(p: Point): GridExtended = {
    copy(
      grid = grid + p,
      anchors = anchors.toArray.transform(a => a + p).toList
    )
  }

  def rotate(angle: Double, pivot: Point = Point.ORIGIN): GridExtended = {
    copy(
      grid = grid.rotate(angle, pivot),
      anchors = anchors.toArray.transform(p => p.rotate(angle, pivot)).toList
    )
  }

  def mirrorVertical(percentage: Double = 1): GridExtended = {
    copy(
      grid = grid.mirrorVertical(percentage),
      anchors = anchors.toArray.transform(p => p.mirrorVertical(percentage)).toList
    )
  }

  def mirrorHorizontal(percentage: Double = 1): GridExtended = {
    copy(
      grid = grid.mirrorHorizontal(percentage),
      anchors = anchors.toArray.transform(p => p.mirrorHorizontal(percentage)).toList
    )
  }

}