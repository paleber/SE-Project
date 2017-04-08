package persistence

import com.db4o.{Db4oEmbedded, ObjectContainer}
import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import scaldi.{Injectable, Injector}

import scala.collection.JavaConversions._

final class Db4oPersistence(implicit inj: Injector) extends Persistence with Injectable {

  private case class Db4oEntry(category: String, name: String, plan: String)

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val databaseName = inject[String]('db4oDatabaseName)

  private def doDatabaseAction[T](f: ObjectContainer => T): T = {
    val db = Db4oEmbedded.openFile(
      Db4oEmbedded.newConfiguration(),
      databaseName
    )
    try {
      f(db)
    } finally {
      db.close()
    }
  }

  private def loadMetaInfo: Map[String, List[String]] = doDatabaseAction { db =>
    val query = db.query()
    query.constrain(classOf[Db4oEntry])
    val set = query.execute[Db4oEntry]()
    val categories: List[String] = set.map(_.category).toList
    categories.map(category => (category, set.filter(_.category == category).map(_.name).toList)).toMap
  }

  override def loadPlan(levelId: LevelId): Plan = doDatabaseAction { db =>
    val query = db.query()
    query.constrain(classOf[Db4oEntry])
    query.descend("category").constrain(levelId.category).equal()
    query.descend("name").constrain(levelId.name).equal()

    val set = query.execute[Db4oEntry]()
    if (set.size != 1) {
      throw new NoSuchElementException("plan not found")
    }
    read[Plan](set.get(0).plan)
  }

  override def savePlan(levelId: LevelId, plan: Plan): Unit = doDatabaseAction { db =>
    val query = db.query()
    query.constrain(classOf[Db4oEntry])
    query.descend("category").constrain(levelId.category).equal()
    query.descend("name").constrain(levelId.name).equal()

    val set = query.execute[Db4oEntry]()
    if (set.size > 0) {
      throw new IllegalArgumentException("LevelId already exists")
    }
    db.store(Db4oEntry(levelId.category, levelId.name, write(plan)))
  }

  override def loadIds: List[LevelId] = ???

  override def removePlan(id: LevelId): Unit = ???
}