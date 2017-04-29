package persistence

import model.element.{LevelId, Plan}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import play.api.libs.ws.WSClient
import scaldi.{Injectable, Injector, Module}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

final case class RestPersistenceModule(url: String) extends Module {

  bind[Persistence] to RestPersistence(url)

}

final case class RestPersistence(url: String)(implicit inj: Injector) extends Persistence with Injectable {

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val ws = inject[WSClient]

  override def loadIds: List[LevelId] = {
    val request = ws.url(s"$url/levelIds").get()
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
    read[List[LevelId]](result.json.toString)
  }

  override def loadPlan(id: LevelId): Plan = {
    val request = ws.url(s"$url/level/${write(id)}").get()
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
    read[Plan](result.json.toString)
  }

  override def savePlan(id: LevelId, plan: Plan): Unit = {
    val request = ws.url(s"$url/level/${write(id)}").put(write(plan))
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
  }

  override def removePlan(id: LevelId): Unit = {
    val request = ws.url(s"$url/level/${write(id)}").delete()
    val result = Await.result(request, 5 seconds)
    if(result.status != 200) {
      throw new IllegalStateException("Status:" + result.status)
    }
  }

}
