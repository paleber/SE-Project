package model.msg

import model.element.{Block, Level}

sealed trait ServerMessage

object ServerMessage {

  case class ShowGame(game: Level) extends ServerMessage

  case object ShowMenu extends ServerMessage

  case class UpdateBlock(index: Int, block: Block) extends ServerMessage

  case object LevelFinished extends ServerMessage

}
