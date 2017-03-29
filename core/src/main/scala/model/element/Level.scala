package model.element

case class Level(id: LevelId,
                 width: Double,
                 height: Double,
                 form: Int,
                 board: Grid,
                 blocks: List[Grid])

case class LevelId(category: String, name: String)
