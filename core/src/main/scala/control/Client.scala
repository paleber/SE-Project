package control


import engine.Point
import model.Level


class Client(views: View*) { // is a actor

  // registerView

  // Interface between server and ui

  // Listener, reactor, tui gui wui

  // receive events from view, sends events to view

}

trait ClientEvent

case object ShowMenu

case class UpdateBlockState(blockId: Int, rotation: Int, position: Point) extends ClientEvent

case class ShowLevel(levelId: Level)



trait ServerEvent

case object MenuShowed

case class BlockStateUpdated(blockId: Int, rotation: Int, position: Point) extends ServerEvent

case class LevelShowed(level: Level)


trait View {

  def receiveEvent(serverEvent: ServerEvent)

}

