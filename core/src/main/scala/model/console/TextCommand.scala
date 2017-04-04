package model.console

import builder.Game._
import model.basic.Point
import model.element.LevelId
import model.msg.ClientMsg
import persistence.ResourceManager.{LoadLevel, LoadMenu}
import tui.Tui


private[console] trait TextCommand {

  val description: String

  val numberArgs: Int

  @throws(classOf[NumberFormatException])
  def parse(args: Array[String]): ClientMsg

}

private[console] object CmdShutdown extends TextCommand {
  override val description = "- Shutdown the application"
  override val numberArgs = 0

  override def parse(args: Array[String]) = Tui.Shutdown
}

private[console] object CmdShowGame extends TextCommand {
  override val description = "category:STRING name:String - Show the game"
  override val numberArgs = 2

  override def parse(args: Array[String]) = LoadLevel(LevelId(args(1), args(2)))
}

private[console] object CmdShowMenu extends TextCommand {
  override val description = "- Show the menu"
  override val numberArgs = 0

  override def parse(args: Array[String]) = LoadMenu
}

private[console] object CmdRotateBlockLeft extends TextCommand {
  override val description = "index:INT - Rotate a block left"
  override val numberArgs = 1

  override def parse(args: Array[String]) = RotateBlockLeft(args(1).toInt)
}

private[console] object CmdRotateBlockRight extends TextCommand {
  override val description = "index:INT - Rotate a block right"
  override val numberArgs = 1

  override def parse(args: Array[String]) = RotateBlockRight(args(1).toInt)
}

private[console] object CmdMirrorBlockVertical extends TextCommand {
  override val description = "index:INT - Mirror a block vertical"
  override val numberArgs = 1

  override def parse(args: Array[String]) = MirrorBlockVertical(args(1).toInt)
}

private[console] object CmdMirrorBlockHorizontal extends TextCommand {
  override val description = "index:INT - Mirror a block horizontal"
  override val numberArgs = 1

  override def parse(args: Array[String]) = MirrorBlockHorizontal(args(1).toInt)
}

private[console] object CmdMoveBlock extends TextCommand {
  override val description = "index:INT x:DOUBLE y:DOUBLE - Move a block"
  override val numberArgs = 3

  override def parse(args: Array[String]) =
    UpdateBlockPosition(args(1).toInt, Point(args(2).toDouble, args(3).toDouble))
}
