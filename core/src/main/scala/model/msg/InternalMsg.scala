package model.msg

sealed trait InternalMsg extends ScongoMsg

object InternalMsg {

  case object GetGame extends InternalMsg

}
