package model.element

case class Level(id: LevelId,
                 form: Int,
                 size: Int,
                 width: Double,
                 height: Double,
                 board: Grid,
                 blocks: List[Grid])

case class LevelId(category: String, name: String)
