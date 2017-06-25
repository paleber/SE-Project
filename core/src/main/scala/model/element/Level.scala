package model.element

case class Level(id: LevelKey,
                 form: Int,
                 size: Int,
                 width: Double,
                 height: Double,
                 board: Grid,
                 blocks: List[Grid])

case class LevelKey(category: String, name: String)
