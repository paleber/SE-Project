package model.plan

import model.plan.GridPlan._

case class LevelPlan(width: Double,
                     height: Double,
                     board: String,
                     blocks: List[String])

/*
object LevelPlan {

  val map = Map(

    "LEVEL4_01A" -> LevelPlan(14, 11, "board4_01",
      List("block4_L5", "block4_L4", "block4_I4")
    ),

    "LEVEL4_01B" -> LevelPlan(14, 11, "board4_01",
      List("block4_L4", "block4_I4", "block4_P5")
    ),

    "LEVEL4_01C" -> LevelPlan(14, 11, "board4_01",
      List("block4_T5", "block4_S4", "block4_L4")
    ),

    "LEVEL4_01D" -> LevelPlan(14, 11, "board4_01",
      List("block4_L5", "block4_I3", "block4_P5")
    ),

    "LEVEL4_01E" -> LevelPlan(14, 11, "board4_01",
      List("block4_L4", "block4_O4", "block4_L5")
    ),

    "LEVEL4_01F" -> LevelPlan(14, 11, "board4_01",
      List("block4_I4", "block4_T4", "block4_T5")
    ),

    "LEVEL6_01A" -> LevelPlan(14, 11, "board6_01",
      List("block6_01", "block6_02", "block6_03")
    )

  )

}
*/