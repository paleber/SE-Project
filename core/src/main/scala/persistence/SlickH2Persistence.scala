package persistence

import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import slick.dbio.NoStream
import slick.jdbc.H2Profile.api._
import slick.lifted.{PrimaryKey, ProvenShape}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try


class SlickH2Persistence extends Persistence {

  private implicit val formats = Serialization.formats(NoTypeHints)

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
    val db = Database.forURL("jdbc:h2:~/scongo3;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "sa")
    try {
      Await.result(db.run(query), 5 seconds)
    } finally {
      db.close()
    }
  }

  def loadPlan(id: LevelId): Plan = {
    val query = plans.filter(plan =>
      plan.category === id.category &&
        plan.name === id.name).map(_.shifts)

    val data = doDatabaseAction(query.result)
    read[Plan](data.head)
  }

  override def loadMetaInfo: Map[String, List[String]] = {
    val query = for (p <- plans) yield (p.category, p.name)
    val data = doDatabaseAction(query.result)

    data.map(_._1).distinct.map(category =>
      (category, data.filter(_._1 == category).map(_._2).toList)
    ).toMap
  }

  override def savePlan(id: LevelId, plan: Plan): Unit = doDatabaseAction(
    DBIO.seq(
      plans += (id.category, id.name, write(plan))
    )
  )

}
