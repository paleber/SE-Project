package model.msg

import control.InitGame
import model.element.{Block, Level}

sealed trait ServerMessage

object ServerMessage {

  case class ShowGame(initGame: InitGame) extends ServerMessage

  case object ShowMenu extends ServerMessage

  case class UpdateBlock(index: Int, block: Block) extends ServerMessage

  case object LevelFinished extends ServerMessage

}
