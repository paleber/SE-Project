package model.element

import model.basic.{Line, Point}

case class Grid(anchors: List[Point],
                polygons: List[List[Point]],
                edges: List[Line],
                position: Point) {

  def +(p: Point): Grid = {
    copy(
      position = position + p
    )
  }

  def rotate(angle: Double, pivot: Point = Point.ZERO): Grid = {
    copy(
      anchors = anchors.map(_.rotate(angle, pivot)),
      polygons = polygons.map(_.map(_.rotate(angle, pivot))),
      edges = edges.map(_.rotate(angle, pivot))
    )
  }

  def mirrorVertical(percentage: Double = 1): Grid = {
    copy(
      anchors = anchors.map(_.mirrorVertical(percentage)),
      polygons = polygons.map(_.map(_.mirrorVertical(percentage))),
      edges = edges.map(_.mirrorVertical(percentage))
    )
  }

  def mirrorHorizontal(percentage: Double = 1): Grid = {
    copy(
      anchors = anchors.map(_.mirrorHorizontal(percentage)),
      polygons = polygons.map(_.map(_.mirrorHorizontal(percentage))),
      edges = edges.map(_.mirrorHorizontal(percentage))
    )
  }

  lazy val absolute: Grid = {
    copy(
      anchors = anchors.map(_ + position),
      polygons = polygons.map(_.map(_ + position)),
      edges = edges.map(_ + position),
      position = Point.ZERO
    )
  }

}
