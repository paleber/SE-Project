package tui

import msg.ClientMessage


trait TextCommand {

  val description: String

  val numberArgs: Int

  /** Execute the command.
    *
    * @param args argument list, including command at args[0] */
  def parse(args: Array[String]): ClientMessage

}
