package persistence

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

import model.element.LevelKey
import model.element.Plan
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.Cursor
import reactivemongo.api.DefaultDB
import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Macros
import scaldi.Injectable
import scaldi.Injector
import scaldi.Module



final class MongoPersistence(uri: String, databaseName: String) extends Persistence  {


  private val connection: Future[MongoConnection] = Future.fromTry(MongoDriver().connection(uri))
  private val database: Future[DefaultDB] = connection.flatMap(_.database(databaseName))

  private val collection: Future[BSONCollection] =
    database.map(_.collection[BSONCollection]("plans"))
      .andThen { case Success(col) =>
        col.indexesManager.ensure(
          Index(
            Seq(
              "category" -> IndexType.Ascending,
              "name" -> IndexType.Ascending
            ),
            unique = true
          )
        )
      }

  private val idProjection = BSONDocument("_id" -> 0, "category" -> 1, "name" -> 1)

  override def readAllKeys(): Future[Set[LevelKey]] = {
    collection.flatMap {
      implicit val reader = Macros.reader[LevelKey]
      _.find(BSONDocument.empty, idProjection).cursor().
        collect(-1, Cursor.FailOnError[List[LevelKey]]())
    }.map(_.toSet)
  }

  private val planProjection = BSONDocument("_id" -> 0, "form" -> 1, "shifts" -> 1)

  override def readPlan(key: LevelKey): Future[Plan] = {
    collection.flatMap {
      implicit val reader = Macros.reader[Plan]
      _.find(BSONDocument("category" -> key.category, "name" -> key.name), planProjection).requireOne
    }
  }

  private case class Entry(category: String, name: String, form: Int, shifts: List[List[List[Int]]])

  override def createPlan(key: LevelKey, plan: Plan): Future[Unit] = {
    collection.flatMap {
      implicit val writer = Macros.writer[Entry]
      _.insert(Entry(key.category, key.name, plan.form, plan.shifts))
    }.flatMap(_ => Future.successful(()))
  }


  override def deletePlan(key: LevelKey): Future[Unit] = {
    collection.flatMap {
      _.remove(BSONDocument("category" -> key.category, "name" -> key.name))
    }.flatMap(result =>
      if (result.n > 0) {
        Future.successful(())
      } else {
        Future.failed(new NoSuchElementException("key not found"))
      }
    )
  }

}
