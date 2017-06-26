
import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import persistence._
import scaldi.Injectable

object PersistenceTransfer extends App with Injectable {

  private val source = new FilePersistence("core/src/main/resources/levels")

  private val target = new MongoPersistence("localhost:24999", "scongo")

  Await.result(
    for {
      keys <- source.readAllKeys()
      plans <- Future.sequence(keys.map(key => source.readPlan(key).map(plan => (key, plan)))).map(_.toMap)
    } yield {
      Future.sequence(plans.keys.map(key => target.createPlan(key, plans(key))))
    },
    Duration(5, TimeUnit.SECONDS)
  )

}
