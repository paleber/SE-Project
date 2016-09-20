package model

import engine.Point


case class Level(width: Double,
                 height: Double,
                 grid: Grid,
                 gridPosition: Point,
                 blocks: List[Block],
                 rotationSteps: Int)
