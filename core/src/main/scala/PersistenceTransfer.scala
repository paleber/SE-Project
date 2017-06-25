
import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import persistence._
import scaldi.Injectable

object PersistenceTransfer extends App with Injectable {

  private val source = {
    implicit val injector = new FilePersistenceModule("core/src/main/resources/levels")
    inject[Persistence]
  }

  private val target = {
    implicit val injector = new MongoPersistenceModule("localhost:24999", "scongo")
    inject[Persistence]
  }

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
