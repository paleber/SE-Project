package modules

import persistence.MongoPersistence
import persistence.Persistence
import scaldi.Module

class CoreModule extends Module {

  bind[Persistence] to new MongoPersistence("localhost:27017", "scongo")

}
