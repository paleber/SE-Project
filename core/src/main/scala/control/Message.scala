package control

import akka.actor.ActorRef
import engine.Point
import model.Level


case class RegisterView(view: ActorRef)

case object ShowMenu

case object ShowGame

case object Shutdown




trait ClientEvent

// case object ShowMenu

case class UpdateBlockState(blockId: Int, rotation: Int, position: Point) extends ClientEvent

case class ShowLevel(levelId: Level)



trait ServerEvent

case object MenuShowed

case class ChangeState(state: AppState)

case class BlockStateUpdated(blockId: Int, rotation: Int, position: Point) extends ServerEvent

case class LevelShowed(level: Level)



