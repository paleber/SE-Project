package model.element

case class LevelPlan(width: Double,
                     height: Double,
                     board: GridPlan,
                     variants: List[List[String]])
