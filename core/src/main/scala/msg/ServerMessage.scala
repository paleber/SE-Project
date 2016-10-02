package msg

import model.{Block, Level}

sealed trait ServerMessage

object ServerMessage {

  case class ShowGame(level: Level) extends ServerMessage

  case object ShowMenu extends ServerMessage

  case class UpdateBlock(index: Int, block: Block) extends ServerMessage

}
