package model.console

import model.msg.ClientMessage


trait TextCommand {

  val description: String

  val numberArgs: Int

  @throws(classOf[NumberFormatException])
  def parse(args: Array[String]): ClientMessage

}

private[console] object CmdShutdown extends TextCommand {
  override val description = "- Shutdown the application"
  override val numberArgs = 0
  override def parse(args: Array[String]) = ClientMessage.Shutdown
}

private[console] object CmdShowGame extends TextCommand {
  override val description = "level:INT - Show the game"
  override val numberArgs = 1
  override def parse(args: Array[String]) = ClientMessage.ShowGame(args(1))
}

private[console] object CmdShowMenu extends TextCommand {
  override val description = "- Show the menu"
  override val numberArgs = 0
  override def parse(args: Array[String]) = ClientMessage.ShowMenu
}
