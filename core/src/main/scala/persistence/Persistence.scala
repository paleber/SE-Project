package persistence

import model.element.{LevelId, Plan}

trait Persistence {

  def loadIds: Seq[LevelId]

  @throws[Exception]
  def loadPlan(id: LevelId): Plan

  @throws[Exception]
  def savePlan(id: LevelId, plan: Plan): Unit

  @throws[Exception]
  def removePlan(id: LevelId): Unit

}
