package model.plan

import model.plan.GridPlan._

case class LevelPlan(width: Double,
                     height: Double,
                     board: GridPlan,
                     blocks: List[GridPlan])

object LevelPlan {

  val map = Map(

    "LEVEL4_01A" -> LevelPlan(14, 11, BOARD4_01,
      List(BLOCK4_L5, BLOCK4_L4, BLOCK4_I4)
    ),

    "LEVEL4_01B" -> LevelPlan(14, 11, BOARD4_01,
      List(BLOCK4_L4, BLOCK4_I4, BLOCK4_P5)
    ),

    "LEVEL4_01C" -> LevelPlan(14, 11, BOARD4_01,
      List(BLOCK4_T5, BLOCK4_S4, BLOCK4_L4)
    ),

    "LEVEL4_01D" -> LevelPlan(14, 11, BOARD4_01,
      List(BLOCK4_L5, BLOCK4_I3, BLOCK4_P5)
    ),

    "LEVEL4_01E" -> LevelPlan(14, 11, BOARD4_01,
      List(BLOCK4_L4, BLOCK4_O4, BLOCK4_L5)
    ),

    "LEVEL4_01F" -> LevelPlan(14, 11, BOARD4_01,
      List(BLOCK4_I4, BLOCK4_T4, BLOCK4_T5)
    ),

    "LEVEL6_01A" -> LevelPlan(14, 11, BOARD6_01,
      List(BLOCK6_01, BLOCK6_02, BLOCK6_03)
    )

  )

}
