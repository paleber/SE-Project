package persistence

import model.element.{LevelKey, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import play.api.libs.ws.WSClient
import scaldi.{Injectable, Injector, Module}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import org.json4s.Formats

final case class RestPersistenceModule(url: String) extends Module {

  bind[Persistence] to RestPersistence(url)

}

final case class RestPersistence(url: String)(implicit inj: Injector) extends Persistence with Injectable {

  private implicit val formats: Formats = Serialization.formats(NoTypeHints)

  private val ws: WSClient = inject[WSClient]

  override def readAllKeys: List[LevelKey] = {
    val request = ws.url(s"$url/levelIds").get()
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
    read[List[LevelKey]](result.json.toString)
  }

  override def readPlan(id: LevelKey): Plan = {
    val request = ws.url(s"$url/level/${write(id)}").get()
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
    read[Plan](result.json.toString)
  }

  override def createPlan(id: LevelKey, plan: Plan): Unit = {
    val request = ws.url(s"$url/level/${write(id)}").put(write(plan))
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
  }

  override def deletePlan(id: LevelKey): Unit = {
    val request = ws.url(s"$url/level/${write(id)}").delete()
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
  }

}
