package msg

import engine.{Grid, Point}
import model.Level

sealed trait ServerMessage

object ServerMessage {

  case class ShowGame(level: Level) extends ServerMessage

  case object ShowMenu extends ServerMessage

  case class UpdateBlock(index: Int, grid: Grid, position: Point) extends ServerMessage

}
