package model.msg

sealed trait InternalMsg

object InternalMsg {

  case object GetGame extends InternalMsg

}
