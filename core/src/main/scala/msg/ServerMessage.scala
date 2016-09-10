package msg

import model.Level

sealed trait ServerMessage

object ServerMessage {

  case class ShowGame(level: Level) extends ServerMessage

  case object ShowMenu

}
