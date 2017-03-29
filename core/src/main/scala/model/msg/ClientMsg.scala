package model.msg

import model.basic.Point
import model.element.LevelId

sealed trait ClientMsg extends ScongoMsg

object ClientMsg {

  case object Shutdown extends ClientMsg

  case object LoadMenu extends ClientMsg

  case class LoadLevel(id: LevelId) extends ClientMsg

  case class RotateBlockLeft(index: Int) extends ClientMsg

  case class RotateBlockRight(index: Int) extends ClientMsg

  case class MirrorBlockVertical(index: Int) extends ClientMsg

  case class MirrorBlockHorizontal(index: Int) extends ClientMsg

  case class UpdateBlockPosition(index: Int, position: Point) extends ClientMsg

}
