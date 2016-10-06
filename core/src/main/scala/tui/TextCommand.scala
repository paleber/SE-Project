package tui

import msg.ClientMessage
import msg.ClientMessage.ShowGame


trait TextCommand {

  val description: String

  val numberArgs: Int

  @throws(classOf[NumberFormatException])
  def parse(args: Array[String]): ClientMessage

}

private[tui] object CmdShutdown extends TextCommand {
  override val description = "- Shutdown the application"
  override val numberArgs = 0
  override def parse(args: Array[String]) = ClientMessage.Shutdown
}

private[tui] object CmdShowGame extends TextCommand {
  override val description = "level:INT - Show the game"
  override val numberArgs = 1
  override def parse(args: Array[String]) = ClientMessage.ShowGame(args(1))
}

private[tui] object CmdShowMenu extends TextCommand {
  override val description = "- Show the menu"
  override val numberArgs = 0
  override def parse(args: Array[String]) = ClientMessage.ShowMenu
}
