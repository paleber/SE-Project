package msg

import akka.actor.ActorRef

sealed trait ClientMessage

object ClientMessage {

  case object Shutdown extends ClientMessage

  case object ShowMenu extends ClientMessage

  case class RegisterView(view: ActorRef) extends ClientMessage

  case class ShowGame(level: Int) extends ClientMessage



}


// case object ShowMenu

//case class UpdateBlockState(blockId: Int, rotation: Int, position: Point) extends ClientMessage

//case class ShowLevel(levelId: Level)




//case object MenuShowed

//case class ChangeState(state: AppState)

//case class BlockStateUpdated(blockId: Int, rotation: Int, position: Point) extends ServerMessage

//case class LevelShowed(level: Level)

