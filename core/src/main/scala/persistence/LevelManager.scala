package persistence

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import model.element.{Level, LevelId}
import model.msg.PersistenceMessages._
import persistence.Persistence.{LevelLoaded, LoadGame, LoadMenu, MenuLoaded}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class LevelManager extends Actor with ActorLogging {
  log.debug("Initializing")

  private implicit val timeout: Timeout = 5 seconds

  private val loader = context.actorOf(FilePersistence.props)

  private val levels = mutable.Map.empty[LevelId, Level]

  private val menuLoadedMsg = Await.result((loader ? LoadMenu).mapTo[MenuLoaded], 5 seconds)

  private case class Request(id: LevelId, sender: ActorRef)

  private val requestBuffer = mutable.ListBuffer.empty[Request]

  override def receive: Receive = {

    case LoadMenu =>
      log.debug("LoadMenu")
      sender ! menuLoadedMsg

    case LoadGame(id) =>
      val level = levels.get(id)
      if (level.isDefined) {
        sender ! LevelLoaded(level.get)
      } else {
        loader ! LoadGame(id)
        requestBuffer += Request(id, sender)
      }

    case LevelLoaded(level) =>
      levels.update(level.id, level)
      val request = requestBuffer.find(_.id == level.id)
      if (request.isDefined) {
        request.get.sender ! LevelLoaded(level)
        requestBuffer -= request.get
      } else {
        log.error("LevelLoaded - request not buffered")
      }

    case LoadingLevelFailed(id) =>
      val request = requestBuffer.find(_.id == id)
      if (request.isDefined) {
        request.get.sender ! LoadingLevelFailed(id)
        requestBuffer -= request.get
      } else {
        log.error("LevelNotFound - request not buffered")
      }

    // lädt den Levelplan
    case SaveLevel => // TODO save the level
    // add the level here

  }


  // Hält die Pläne für jedes Level, sobald es zum ersten mal angeforedet wird oder geschrieben wird

  // Beim schreiben wird gehalten
  // Beim lesen neu geladen


}
