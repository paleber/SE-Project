package model.msg

import model.element.{LevelId, Plan}


object PersistenceMessages {

  case class LoadingLevelFailed(id: LevelId)

  case class SaveLevel(id: LevelId, plan: Plan)

  case class LevelSaved(id: LevelId)

  case class LevelAlreadyExists(id: LevelId)

}
