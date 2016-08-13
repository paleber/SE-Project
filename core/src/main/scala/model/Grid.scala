package model

import engine.{Line, Point}

case class Grid(anchors: Array[Point],
                border: Array[Line],
                lines: Array[Line])