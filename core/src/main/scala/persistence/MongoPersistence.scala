package persistence

import model.element.{LevelKey, Plan}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{Cursor, MongoDriver}
import reactivemongo.bson.{BSONDocument, Macros}
import scaldi.{Injectable, Injector, Module}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import reactivemongo.api.MongoConnection


final case class MongoPersistenceModule(uri: String, database: String) extends Module {

  bind[Persistence] to new MongoPersistence
  bind[String] identifiedBy 'mongoPersistenceUri to uri
  bind[String] identifiedBy 'mongoPersistenceDatabase to database

}

private final class MongoPersistence(implicit inj: Injector) extends Persistence with Injectable {

  private val connection: Future[MongoConnection] = Future.fromTry {
    MongoDriver().connection(inject[String]('mongoPersistenceUri))
  }

  private val databaseName: String = inject[String]('mongoPersistenceDatabase)

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

  override def readAllKeys: List[LevelKey] = doPlanCollectionAction {
    implicit val reader = Macros.reader[LevelKey]
    _.find(BSONDocument.empty, idProjection).cursor().
      collect(-1, Cursor.FailOnError[List[LevelKey]]())
  }


  private val planProjection = BSONDocument("_id" -> 0, "form" -> 1, "shifts" -> 1)

  override def readPlan(id: LevelKey): Plan = doPlanCollectionAction {
    implicit val reader = Macros.reader[Plan]
    _.find(BSONDocument("category" -> id.category, "name" -> id.name), planProjection).requireOne
  }


  private case class Entry(category: String, name: String, form: Int, shifts: List[List[List[Int]]])

  override def createPlan(id: LevelKey, plan: Plan): Unit = doPlanCollectionAction {
    implicit val writer = Macros.writer[Entry]
    _.insert(Entry(id.category, id.name, plan.form, plan.shifts))
  }


  override def deletePlan(id: LevelKey): Unit = {
    val result = doPlanCollectionAction {
      _.remove(BSONDocument("category" -> id.category, "name" -> id.name))
    }
    if(result.n != 1) {
      throw new NoSuchElementException("id not found")
    }
  }

}
