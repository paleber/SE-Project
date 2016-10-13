package model.element

case class Game(levelName: String,
                width: Double,
                height: Double,
                form: Int,
                board: Grid,
                blocks: List[Block])
