package persistence

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.db4o.Db4oEmbedded
import com.db4o.ObjectContainer
import model.element.LevelKey
import model.element.Plan
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write
import scaldi.Injectable


final class Db4oPersistence(path: String) extends Persistence with Injectable {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private case class Db4oEntry(category: String, name: String, plan: String)

  private val db: ObjectContainer = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), path)

  override def readPlan(key: LevelKey): Future[Plan] = {
    Future {
      val query = db.query()
      query.constrain(classOf[Db4oEntry])
      query.descend("category").constrain(key.category).equal()
      query.descend("name").constrain(key.name).equal()

      val set = query.execute[Db4oEntry]
      if (set.isEmpty) {
        throw new NoSuchElementException("plan not found")
      }
      read[Plan](set.head.plan)
    }
  }

  override def createPlan(key: LevelKey, plan: Plan): Future[Unit] = {
    Future {
      val query = db.query()
      query.constrain(classOf[Db4oEntry])
      query.descend("category").constrain(key.category).equal()
      query.descend("name").constrain(key.name).equal()

      val set = query.execute[Db4oEntry]
      if (set.size > 0) {
        throw new IllegalArgumentException("key already exists")
      }
      db.store(Db4oEntry(key.category, key.name, write(plan)))
    }
  }

  override def readAllKeys(): Future[Set[LevelKey]] = {
    Future {
      val query = db.query()
      query.constrain(classOf[Db4oEntry])
      val set = query.execute[Db4oEntry]
      set.map(entry => LevelKey(entry.category, entry.name)).toSet
    }
  }

  override def deletePlan(id: LevelKey): Future[Unit] = {
    Future {
      val query = db.query()
      query.constrain(classOf[Db4oEntry])
      query.descend("category").constrain(id.category).equal()
      query.descend("name").constrain(id.name).equal()

      val set = query.execute[Db4oEntry]
      if (set.isEmpty) {
        throw new NoSuchElementException("id not found")
      }
      db.delete(set.head)
    }
  }

}
