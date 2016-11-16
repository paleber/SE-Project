package model.msg

import model.element.{Block, Game}

sealed trait ServerMsg extends ScongoMsg

object ServerMsg {

  case object ShowMenu extends ServerMsg

  case class ShowGame(game: Game) extends ServerMsg

  case class UpdateBlock(index: Int, block: Block) extends ServerMsg

  case class LevelFinished(timeMillis: Int) extends ServerMsg

}
