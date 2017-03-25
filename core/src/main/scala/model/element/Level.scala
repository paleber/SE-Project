package model.element

case class Level(category: String,
                 name: String,
                 width: Double,
                 height: Double,
                 form: Int,
                 board: AnchoredGrid,
                 blocks: List[AnchoredGrid])
