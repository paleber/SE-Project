package control

sealed trait AppState

object AppState {

  case object Menu extends AppState

  case object Game extends AppState

}
