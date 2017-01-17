package model.console

import akka.actor.{Actor, ActorLogging}
import model.msg.ParserMsg



private object CmdParser {

  val cmdMap = Map(
    "exit" -> CmdShutdown,
    "menu" -> CmdShowMenu,
    "game" -> CmdShowGame,
    "left" -> CmdRotateBlockLeft,
    "right" -> CmdRotateBlockRight,
    "vertical" -> CmdMirrorBlockVertical,
    "horizontal" -> CmdMirrorBlockHorizontal,
    "move" -> CmdMoveBlock
  )

}

class CmdParser extends Actor with ActorLogging {

  override def receive: PartialFunction[Any, Unit] = {

    case input: String =>
      parseInput(input)

    case msg =>
      log.warning("Unhandled message: " + msg)

  }

  private def parseInput(input: String): Unit = {
    log.debug("Parsing input: " + input)

    if (input == "help") {
      context.parent ! ParserMsg("help - print this help")
      for ((key, cmd) <- CmdParser.cmdMap) {
        context.parent ! ParserMsg(s"$key ${cmd.description}")
      }
      return
    }

    val args = input.split(" ")
    if (args.isEmpty) {
      return
    }

    val cmd = CmdParser.cmdMap.get(args(0))

    if (cmd.isEmpty) {
      context.parent ! ParserMsg(s"Unknown command '${args(0)}', type 'help' to print available commands")
      return
    }

    if (args.length - 1 != cmd.get.numberArgs) {
      context.parent ! ParserMsg(s"Command '${args(0)}' requires ${cmd.get.numberArgs} arguments")
      return
    }

    try {
      val msg = cmd.get.parse(args)
      context.parent ! msg
    } catch {
      case e: NumberFormatException =>
        context.parent ! ParserMsg(s"Wrong argument format of command '${args(0)}'")
    }
  }

}
