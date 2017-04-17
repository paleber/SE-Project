import persistence.{FilePersistence, MongoPersistence, Persistence}
import scaldi.{Injectable, Module}

object PersistenceTransfer extends App with Injectable {

  private val source = {
    implicit object Injector extends Module {
      bind[Persistence] to new FilePersistence
      bind[String] identifiedBy 'filePath to "core/src/main/resources/levels"
    }
    inject[Persistence]
  }


  private val target = {
    implicit object Injector extends Module {
      bind[Persistence] to new MongoPersistence
      bind[String] identifiedBy 'mongoUri to "localhost:24999"
      bind[String] identifiedBy 'mongoDatabase to "scongo"
    }
    inject[Persistence]
  }

  source.loadIds.foreach(id => {
    target.savePlan(id, source.loadPlan(id))
  })

}
