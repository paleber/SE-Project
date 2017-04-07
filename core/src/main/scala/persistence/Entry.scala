package persistence

import model.element.Plan
import reactivemongo.bson.Macros

case class Entry(category: String = null, name: String = null, plan: Plan = null)

object Entry {
  implicit def handler = Macros.handler[Plan]
}