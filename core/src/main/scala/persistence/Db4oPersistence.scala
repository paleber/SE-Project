package persistence

import com.db4o.{Db4oEmbedded, ObjectContainer}
import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

import scala.util.Try


class Db4oPersistence extends Persistence {

  private case class Db4oEntry(category: String, name: String, plan: String)

  private implicit val formats = Serialization.formats(NoTypeHints)

  private def openDatabase(): ObjectContainer = {
    Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "db4o-database")
  }

  override def loadMetaInfo: Map[String, List[String]] = {
    // TODO
    null
  }

  override def loadPlan(levelId: LevelId): Plan = {

    val db = openDatabase()
    val query = db.query()
    query.constrain(classOf[Db4oEntry])
    query.descend("category").constrain(levelId.category).equal()
    query.descend("name").constrain(levelId.name).equal()

    val set = query.execute[Db4oEntry]()
    if (set.size != 1) {
      db.close()
      throw new NoSuchElementException("plan not found")
    }

    val plan = read[Plan](set.get(0).plan)
    db.close()
    plan

  }

  override def savePlan(levelId: LevelId, plan: Plan): Unit = {
    if (Try(loadPlan(levelId)).isSuccess) {
      throw new IllegalArgumentException("LevelId already exists")
    }

    val db = openDatabase()
    db.store(Db4oEntry(levelId.category, levelId.name, write(plan)))
    db.close()
  }





}


object x extends App {

  val p = new Db4oPersistence


  val y = p.loadPlan(LevelId("abc", "lv1"))
  println(y)

}

object a extends App {

  val p = new Db4oPersistence

  val plan = Plan(4, List(


    List(List(3, 0), List(3)),
    List(List(2), List(2, 1))
  ))

  println(plan)

  p.savePlan(
    LevelId("abc", "lv1"),
    plan
  )


}

