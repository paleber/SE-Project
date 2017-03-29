package model.msg

import model.element.{Grid, Level}

sealed trait ServerMsg extends ScongoMsg

object ServerMsg {

  case class MenuLoaded(info: Map[String, List[String]]) extends ServerMsg

  case class LevelLoaded(level: Level) extends ServerMsg

  case class BlockUpdated(index: Int, block: Grid) extends ServerMsg

  case class LevelFinished(timeMillis: Int) extends ServerMsg

}
