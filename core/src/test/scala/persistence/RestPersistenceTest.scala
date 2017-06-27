package persistence

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient


class RestPersistenceTest extends PersistenceBehavior {

  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()

  "restPersistence" should behave like persistenceBehavior(
    new RestPersistence("http://localhost:9000", AhcWSClient())
  )

}
