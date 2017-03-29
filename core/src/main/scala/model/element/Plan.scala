package model.element

/** A plan to create the board and blocks of a level. */
case class Plan(form: Int, shifts: List[List[List[Int]]])
