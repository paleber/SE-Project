package tui

import java.util.Scanner

import akka.actor.{Actor, ActorLogging, Props}
import msg.{ClientMessage, ServerMessage}




case class ConsoleInput(cmd: String)

case class ConsoleOutput(cmd: String)

private object ConsoleCmdParser {

  val cmdMap = Map(
    "exit" -> CmdShutdown,
    "menu" -> CmdShowMenu,
    "game" -> CmdShowGame
  )

}

class TextCmdParser extends Actor with ActorLogging {

  override def receive = {

    case ConsoleInput(input: String) =>
      parseInput(input)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  private def parseInput(input: String): Unit = {
    log.debug("Parsing input: " + input)

    if (input == "help") {
      context.parent ! ConsoleOutput("help - print this help")
      for ((key, cmd) <- ConsoleCmdParser.cmdMap) {
        context.parent ! ConsoleOutput(s"$key ${cmd.description}")
      }
      return
    }

    val args = input.split(" ")
    if (args.isEmpty) {
      return
    }

    val cmd = ConsoleCmdParser.cmdMap.get(args(0))

    if (cmd.isEmpty) {
      context.parent ! ConsoleOutput(s"Unknown command '${args(0)}', type 'help' to print available commands")
      return
    }

    if (args.length - 1 != cmd.get.numberArgs) {
      context.parent ! ConsoleOutput(s"Command '${args(0)}' requires ${cmd.get.numberArgs} arguments")
      return
    }

    try {
      val msg = cmd.get.parse(args)
      context.parent ! msg
    } catch {
      case e: NumberFormatException =>
        context.parent ! ConsoleOutput(s"Wrong argument format of command '${args(0)}'")
    }
  }

}







