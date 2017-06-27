package modules

import persistence.FilePersistence
import persistence.Persistence
import scaldi.Module

class CoreModule extends Module {

  bind[Persistence] to new FilePersistence("core/src/main/resources/levels")

}
