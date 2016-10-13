package model.element

case class Level(name: String,
                 width: Double,
                 height: Double,
                 board: GridExtended,
                 blocks: List[GridExtended])