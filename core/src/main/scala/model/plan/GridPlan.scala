package model.plan

case class GridPlan(rotationSteps: Int, shifts: List[List[Int]])

object GridPlan {

  lazy val BLOCK4_I2 = GridPlan(4, List(
    List(1)
  ))

  lazy val BLOCK4_I3 = GridPlan(4, List(
    List(1),
    List(3)
  ))

  lazy val BLOCK4_L3 = GridPlan(4, List(
    List(1),
    List(2)
  ))

  lazy val BLOCK4_I4 = GridPlan(4, List(
    List(3),
    List(1),
    List(1, 1)
  ))

  lazy val BLOCK4_O4 = GridPlan(4, List(
    List(1),
    List(2),
    List(1, 2)
  ))

  lazy val BLOCK4_L4 = GridPlan(4, List(
    List(3),
    List(1),
    List(1, 0)
  ))

  lazy val BLOCK4_S4 = GridPlan(4, List(
    List(1),
    List(2),
    List(2, 3)
  ))

  lazy val BLOCK4_T4 = GridPlan(4, List(
    List(0),
    List(1),
    List(3)
  ))

  lazy val BLOCK4_P5 = GridPlan(4, List(
    List(0),
    List(0, 1),
    List(1),
    List(3)
  ))

  lazy val BLOCK4_L5 = GridPlan(4, List(
    List(3),
    List(3, 0),
    List(1),
    List(1, 1)
  ))

  lazy val BLOCK4_T5 = GridPlan(4, List(
    List(0),
    List(3),
    List(3, 3),
    List(1)
  ))

  lazy val BLOCK4_S5 = GridPlan(4, List(
    List(3),
    List(3, 2),
    List(1),
    List(1, 0)
  ))

  lazy val BOARD4_01 = GridPlan(4, List(
    List(0, 3),
    List(0),
    List(0, 1),
    List(0, 1, 1),
    List(3),
    List(1),
    List(1, 1),
    List(2, 3, 3, 3),
    List(2, 3, 3),
    List(2, 3),
    List(2),
    List(2, 1)
  ))

  lazy val BOARD6_01 = GridPlan(6, List(
    List(0),
    List(1),
    List(2),
    List(4),
    List(5)
  ))

  lazy val BLOCK6_01 = GridPlan(6, List.empty)

  lazy val BLOCK6_02 = GridPlan(6, List(
    List(2)
  ))

  lazy val BLOCK6_03 = GridPlan(6, List(
    List(1),
    List(3)
  ))

}
