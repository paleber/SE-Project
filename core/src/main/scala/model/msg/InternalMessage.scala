package model.msg

sealed trait InternalMessage

object InternalMessage {

  case object GetGame extends InternalMessage

}
