import persistence._
import scaldi.Injectable

object PersistenceTransfer extends App with Injectable {

  private val source = {
    implicit val injector =  FilePersistenceModule("core/src/main/resources/levels")
    inject[Persistence]
  }

  private val target = {
    implicit val injector = MongoPersistenceModule("localhost:24999", "scongo")
    inject[Persistence]
  }

  source.loadIds.foreach(id => {
    target.savePlan(id, source.loadPlan(id))
  })

}
