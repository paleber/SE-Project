package model

import engine.{Line, Point}

case class Grid(anchors: List[Point],
                corners: List[Point],
                lines: List[Line])