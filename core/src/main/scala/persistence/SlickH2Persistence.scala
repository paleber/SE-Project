package persistence

import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import scaldi.{Injectable, Injector, Module}
import slick.dbio.NoStream
import slick.jdbc.H2Profile.api._
import slick.lifted.{PrimaryKey, ProvenShape}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try


final case class SlickH2PersistenceModule(database: String) extends Module {

  bind[Persistence] to new SlickH2Persistence
  bind[String] identifiedBy 'slickH2PersistenceDatabase to database

}

private final class SlickH2Persistence(implicit inj: Injector) extends Persistence with Injectable{

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val databaseName = inject[String]('slickH2PersistenceDatabase)

  private class Plans(tag: Tag) extends Table[(String, String, String)](tag, "PLANS") {

    def category: Rep[String] =
      column[String]("CATEGORY")

    def name: Rep[String] =
      column[String]("NAME")

    def shifts: Rep[String] =
      column[String]("SHIFTS")

    def pk: PrimaryKey =
      primaryKey("pk", (category, name))

    // Every table needs a * projection with the same type as the table's type parameter
    def * : ProvenShape[(String, String, String)] =
      (category, name, shifts)
  }

  private val plans = TableQuery[Plans]

  Try(doDatabaseAction(DBIO.seq(
    plans.schema.create
  )))

  private def doDatabaseAction[R](query: DBIOAction[R, NoStream, Nothing]): R = {
    val db = Database.forURL(s"jdbc:h2:~/$databaseName;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "sa")
    try {
      Await.result(db.run(query), 5 seconds)
    } finally {
      db.close()
    }
  }

  override def loadPlan(id: LevelId): Plan = {
    val query = plans.filter(plan =>
      plan.category === id.category &&
        plan.name === id.name).map(_.shifts)

    val data = doDatabaseAction(query.result)
    read[Plan](data.head)
  }

  override def savePlan(id: LevelId, plan: Plan): Unit = doDatabaseAction(
    DBIO.seq(
      plans += (id.category, id.name, write(plan))
    )
  )

  override def loadIds: Seq[LevelId] = {
    val query = for (p <- plans) yield (p.category, p.name)
    doDatabaseAction(query.result).map {
      case (category, name) => LevelId(category, name)
    }
  }

  override def removePlan(id: LevelId): Unit = {
    val query = plans.filter(plan =>
      plan.category === id.category &&
        plan.name === id.name).delete

    if(doDatabaseAction(query) != 1) {
      throw new IllegalStateException("id not found")
    }
  }

}
