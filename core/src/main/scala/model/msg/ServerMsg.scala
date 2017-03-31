package model.msg

import model.element.{Grid, Level}

trait ServerMsg extends ScongoMsg

object ServerMsg {

  case class BlockUpdated(index: Int, block: Grid) extends ServerMsg

  case class LevelFinished(timeMillis: Int) extends ServerMsg

}
