package model.element

case class Level(name: String,
                 width: Double,
                 height: Double,
                 form: Int,
                 board: Grid,
                 blocks: List[Block])
