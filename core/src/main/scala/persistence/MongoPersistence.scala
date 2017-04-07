package persistence

import model.element.{LevelId, Plan}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{Cursor, MongoDriver}
import reactivemongo.bson.{BSONDocument, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object MongoPersistence extends Persistence {

  private val connection = Future.fromTry {
    MongoDriver().connection("localhost")
  }

  private def doPlanCollectionAction[T](f: BSONCollection => Future[T]): T = {
    val collection = connection.flatMap(_.database("scongo")).map(_.collection("plans"))
    Await.result(collection.flatMap(f), 5.seconds)
  }

  doPlanCollectionAction {
    _.indexesManager.ensure(Index(Seq(
      ("id.category", IndexType.Ascending),
      ("id.name", IndexType.Ascending)),
      unique = true))
  }


  private val idProjection = BSONDocument("_id" -> 0, "category" -> 1, "name" -> 1)

  override def loadMetaInfo: Map[String, List[String]] = {
    implicit val reader = Macros.reader[LevelId]
    val entries = doPlanCollectionAction {
      _.find(BSONDocument.empty, idProjection).cursor().
        collect(-1, Cursor.FailOnError[List[LevelId]]())
    }
    entries.map(_.category).distinct.map(cat => (cat, entries.filter(_.category == cat).map(_.name))).toMap
  }


  private case class PlanWrapper(plan: Plan)

  private val planProjection = BSONDocument("_id" -> 0, "plan" -> 1)

  override def loadPlan(id: LevelId): Plan = {
    implicit val reader = {
      implicit val planReader = Macros.reader[Plan]
      Macros.reader[PlanWrapper]
    }

    doPlanCollectionAction {
      _.find(BSONDocument("category" -> id.category, "name" -> id.name), planProjection).requireOne
    }.plan
  }


  override def savePlan(id: LevelId, plan: Plan): Unit = doPlanCollectionAction { col =>
    implicit val writer = Macros.writer[Plan]
    col.insert(BSONDocument(
      "category" -> id.category,
      "name" -> id.name,
      "plan" -> plan
    ))
  }

}
