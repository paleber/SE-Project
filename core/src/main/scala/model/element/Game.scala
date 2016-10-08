package model.element

case class Game(name: String,
                width: Double,
                height: Double,
                board: GridExtended,
                blocks: List[GridExtended])