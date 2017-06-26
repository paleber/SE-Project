package persistence

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

import model.element.LevelKey
import model.element.Plan
import org.json4s.Formats
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.read
import org.json4s.jackson.Serialization.write
import play.api.libs.ws.WSClient


class RestPersistence(url: String, ws: WSClient) extends Persistence {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  override def readAllKeys(): Future[Set[LevelKey]] = {
    ws.url(s"$url/levelIds").get().flatMap(result =>
      if (result.status == 200) {
        Future.failed(new IllegalStateException("Status:" + result.status))
      } else {
        Future.successful(read[Set[LevelKey]](result.json.toString))
      }
    )
  }

  override def readPlan(key: LevelKey): Future[Plan] = {
    ws.url(s"$url/level/${write(key)}").get().flatMap(result =>
      if (result.status == 200) {
        Future.failed(new IllegalStateException("Status:" + result.status))
      } else {
        Future.successful(read[Plan](result.json.toString))
      }
    )
  }

  override def createPlan(key: LevelKey, plan: Plan): Future[Unit] = {
    ws.url(s"$url/level/${write(key)}").put(write(plan)).flatMap(result =>
      if (result.status == 200) {
        Future.failed(new IllegalStateException("Status:" + result.status))
      } else {
        Future.successful(())
      }
    )
  }

  override def deletePlan(key: LevelKey): Future[Unit] = {
    ws.url(s"$url/level/${write(key)}").delete().flatMap(result =>
      if (result.status == 200) {
        Future.failed(new IllegalStateException("Status:" + result.status))
      } else {
        Future.successful(())
      }
    )
  }

}
