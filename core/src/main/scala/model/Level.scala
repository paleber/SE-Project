package model


case class Level(width: Double,
                 height: Double,
                 board: Grid,
                 blocks: List[Block],
                 rotationSteps: Int)
