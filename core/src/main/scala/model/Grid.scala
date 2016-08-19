package model

import engine.{Line, Point}

case class Grid(anchors: Array[Point],
                corners: Array[Point],
                lines: Array[Line])