package model

import engine.Grid

case class Level(width: Double,
                 height: Double,
                 board: Grid,
                 blocks: List[Block])
