package persistence


import scala.language.postfixOps

import slick.jdbc.H2Profile.api._

class SlickPersistenceTest extends PersistenceBehavior {

  "slickH2Persistence" should behave like persistenceBehavior(
    new SlickPersistence(Database.forURL(
      url = "jdbc:h2:~/scongoTest;DB_CLOSE_DELAY=-1",
      driver = "org.h2.Driver",
      user = "sa"
    ))
  )

}
