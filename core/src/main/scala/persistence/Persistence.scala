package persistence

import scala.concurrent.Future

import model.element.LevelKey
import model.element.Plan

trait Persistence {

  /** Create a plan.
   *
   * @param key  the key of the plan
   * @param plan the plan to create
   * @return Unit-Future
   */
  def createPlan(key: LevelKey, plan: Plan): Future[Unit]

  /** Read a plan.
   *
   * @param key the key of the plan
   * @return Future with the plan
   */
  def readPlan(key: LevelKey): Future[Plan]

  /** Delete a plan.
   *
   * @param key the key of the plan
   * @return Unit-Future
   */
  def deletePlan(key: LevelKey): Future[Unit]

  /** Read all keys.
   *
   * @return all keys
   */
  def readAllKeys(): Future[Set[LevelKey]]

}
