package model.element

import model.basic.Point

case class AnchoredGrid(grid: Grid,
                        form: Int,
                        anchors: List[Point]) {

  def +(p: Point): AnchoredGrid = {
    copy(
      grid = grid + p,
      anchors = anchors.toArray.transform(a => a + p).toList
    )
  }

  def rotate(angle: Double, pivot: Point = Point.ORIGIN): AnchoredGrid = {
    copy(
      grid = grid.rotate(angle, pivot),
      anchors = anchors.toArray.transform(p => p.rotate(angle, pivot)).toList
    )
  }

  def mirrorVertical(percentage: Double = 1): AnchoredGrid = {
    copy(
      grid = grid.mirrorVertical(percentage),
      anchors = anchors.toArray.transform(p => p.mirrorVertical(percentage)).toList
    )
  }

  def mirrorHorizontal(percentage: Double = 1): AnchoredGrid = {
    copy(
      grid = grid.mirrorHorizontal(percentage),
      anchors = anchors.toArray.transform(p => p.mirrorHorizontal(percentage)).toList
    )
  }

}