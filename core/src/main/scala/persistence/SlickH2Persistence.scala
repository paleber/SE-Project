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
import persistence.SlickH2Persistence._
import slick.dbio.NoStream
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.lifted.PrimaryKey
import slick.lifted.ProvenShape

private object SlickH2Persistence {

  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  class Plans(tag: Tag) extends Table[(String, String, String)](tag, "PLANS") {

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

  val plans: TableQuery[Plans] = TableQuery[Plans]

}

final class SlickH2Persistence(database: H2Profile.backend.DatabaseDef) extends Persistence {

  private val dbReady: Future[Unit] = database.run(plans.schema.create).recoverWith { case _ => Future.successful(()) }

  private def doDatabaseAction[R](query: DBIOAction[R, NoStream, Nothing]): Future[R] = {
    dbReady.flatMap(_ =>
      database.run(query)
    )
  }

  override def createPlan(key: LevelKey, plan: Plan): Future[Unit] = {
    doDatabaseAction(
      plans += (key.category, key.name, write(plan))
    ).flatMap(_ => Future.successful(()))
  }

  override def readPlan(key: LevelKey): Future[Plan] = {
    doDatabaseAction(
      plans.filter(plan =>
        plan.category === key.category && plan.name === key.name
      ).map(_.shifts).result
    ).map(data =>
      read[Plan](data.head)
    )
  }

  override def deletePlan(key: LevelKey): Future[Unit] = {
    doDatabaseAction(
      plans.filter(plan =>
        plan.category === key.category && plan.name === key.name
      ).delete
    ).flatMap(result =>
      if (result > 0) {
        Future.successful(())
      } else {
        Future.failed(new IllegalStateException("key not found"))
      }
    )
  }

  override def readAllKeys(): Future[Set[LevelKey]] = {
    doDatabaseAction(
      plans.map(plan =>
        (plan.category, plan.name)
      ).result
    ).map(_.map {
      case (category, name) => LevelKey(category, name)
    }).map(_.toSet)
  }

}
