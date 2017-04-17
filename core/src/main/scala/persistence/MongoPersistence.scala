package persistence

import model.element.{LevelId, Plan}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{Cursor, MongoDriver}
import reactivemongo.bson.{BSONDocument, Macros}
import scaldi.{Injectable, Injector}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


final class MongoPersistence(implicit inj: Injector) extends Persistence with Injectable {

  private val databaseName = inject[String]('mongoDatabase)
  private val connection = Future.fromTry {
    MongoDriver().connection(inject[String]('mongoUri))
  }

  private def doPlanCollectionAction[T](f: BSONCollection => Future[T]): T = {
    val collection = connection.flatMap(_.database(databaseName)).map(_.collection("plans"))
    Await.result(collection.flatMap(f), 15.seconds)
  }

  doPlanCollectionAction {
    _.indexesManager.ensure(Index(Seq(
      ("category", IndexType.Ascending),
      ("name", IndexType.Ascending)),
      unique = true))
  }


  private val idProjection = BSONDocument("_id" -> 0, "category" -> 1, "name" -> 1)

  override def loadIds: List[LevelId] = doPlanCollectionAction {
    implicit val reader = Macros.reader[LevelId]
    _.find(BSONDocument.empty, idProjection).cursor().
      collect(-1, Cursor.FailOnError[List[LevelId]]())
  }


  private val planProjection = BSONDocument("_id" -> 0, "form" -> 1, "shifts" -> 1)

  override def loadPlan(id: LevelId): Plan = doPlanCollectionAction {
    implicit val reader = Macros.reader[Plan]
    _.find(BSONDocument("category" -> id.category, "name" -> id.name), planProjection).requireOne
  }


  private case class Entry(category: String, name: String, form: Int, shifts: List[List[List[Int]]])

  override def savePlan(id: LevelId, plan: Plan): Unit = doPlanCollectionAction {
    implicit val writer = Macros.writer[Entry]
    _.insert(Entry(id.category, id.name, plan.form, plan.shifts))
  }


  override def removePlan(id: LevelId): Unit = {
    val result = doPlanCollectionAction {
      _.remove(BSONDocument("category" -> id.category, "name" -> id.name))
    }
    if(result.n != 1) {
      throw new NoSuchElementException("id not found")
    }
  }

}
