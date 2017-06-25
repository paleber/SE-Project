package persistence

import scala.concurrent.Future

import model.element.LevelKey
import model.element.Plan

trait Persistence {

  def readAllKeys(): Future[Seq[LevelKey]]

  def readPlan(key: LevelKey): Future[Plan]

  def createPlan(key: LevelKey, plan: Plan): Future[Unit]

  def deletePlan(key: LevelKey): Future[Unit]

}
