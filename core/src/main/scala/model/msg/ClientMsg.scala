package model.msg

import akka.actor.ActorRef
import model.basic.Point

sealed trait ClientMsg extends ScongoMsg

object ClientMsg {

  case object Shutdown extends ClientMsg

  case object ShowMenu extends ClientMsg

  case class RegisterView(view: ActorRef) extends ClientMsg

  case class ShowGame(level: String) extends ClientMsg

  case class RotateBlockLeft(index: Int) extends ClientMsg

  case class RotateBlockRight(index: Int) extends ClientMsg

  case class MirrorBlockVertical(index: Int) extends ClientMsg

  case class MirrorBlockHorizontal(index: Int) extends ClientMsg

  case class UpdateBlockPosition(index: Int, position: Point) extends ClientMsg

}
