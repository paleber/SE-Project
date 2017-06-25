package persistence

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

import model.element.LevelKey
import model.element.Plan
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write
import scaldi.Injectable
import scaldi.Injector
import scaldi.Module
import slick.dbio.NoStream
import slick.jdbc.H2Profile.api._
import slick.lifted.PrimaryKey
import slick.lifted.ProvenShape


final class SlickH2PersistenceModule(database: String) extends Module {

  bind[Persistence] to new SlickH2Persistence
  bind[String] identifiedBy 'slickH2PersistenceDatabase to database

}

private final class SlickH2Persistence(implicit inj: Injector) extends Persistence with Injectable {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private val databaseName: String = inject[String]('slickH2PersistenceDatabase)

  private class Plans(tag: Tag) extends Table[(String, String, String)](tag, "PLANS") {

    def category: Rep[String] = {
      column[String]("CATEGORY")
    }

    def name: Rep[String] = {
      column[String]("NAME")
    }

    def shifts: Rep[String] = {
      column[String]("SHIFTS")
    }

    def pk: PrimaryKey = {
      primaryKey("pk", (category, name))
    }

    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(String, String, String)] = {
      (category, name, shifts)
    }

  }

  private val plans: TableQuery[Plans] = TableQuery[Plans]

  private val schemasCreated: Future[Unit] = doDatabaseAction(plans.schema.create)

  private def doDatabaseAction[R](query: DBIOAction[R, NoStream, Nothing]): Future[R] = {
    val db = Database.forURL(s"jdbc:h2:~/$databaseName;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "sa")
    schemasCreated.flatMap(_ =>
      db.run(query)
    ).andThen {
      case _ => db.close()
    }
  }

  override def readPlan(key: LevelKey): Future[Plan] = {
    val query = plans.filter(plan => plan.category === key.category && plan.name === key.name).map(_.shifts)
    doDatabaseAction(query.result).map {
      data =>
        read[Plan](data.head)
    }
  }

  override def createPlan(key: LevelKey, plan: Plan): Future[Unit] = {
    doDatabaseAction(
     plans += (key.category, key.name, write(plan))
    ).flatMap(_ => Future.successful(()))
  }

  override def readAllKeys(): Future[Set[LevelKey]] = {
    val query = plans.map(plan => (plan.category, plan.name))
    doDatabaseAction(query.result).map(_.map {
      case (category, name) => LevelKey(category, name)
    }).map(_.toSet)
  }

  override def deletePlan(key: LevelKey): Future[Unit] = {
    val query = plans.filter(plan => plan.category === key.category && plan.name === key.name).delete
    doDatabaseAction(query).flatMap(result =>
      if (result > 0) {
        Future.successful(())
      } else {
        Future.failed(new IllegalStateException("key not found"))
      }
    )
  }

}
