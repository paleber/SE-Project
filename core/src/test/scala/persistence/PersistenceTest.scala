package persistence

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import model.element.LevelKey
import model.element.Plan
import org.scalatest.AsyncFlatSpec
import org.scalatest.Matchers
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import scaldi.Injectable
import scaldi.Injector
import scaldi.Module

class PersistenceTest extends AsyncFlatSpec with Matchers with Injectable {

  private val key1 = LevelKey("cat1", "lv1")
  private val plan1 = Plan(4, List(List(List())))

  private val key2 = LevelKey("cat1", "lv2")
  private val plan2 = Plan(4, List(List(List(1, 2), List(3, 2)), List(List(1, 2, 2)), List(List(2, 3))))

  private val key3 = LevelKey("cat2", "lv1")
  private val plan3 = Plan(6, List(List(List(1))))

  def persistenceBehavior(implicit injector: Injector): Unit = {

    val persistence = inject[Persistence]

    it should "remove all already existing plans" in {
      for {
        existingKeys <- persistence.readAllKeys()
        _ <- Future.sequence(existingKeys.map(persistence.deletePlan))
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set.empty
      }
    }

    it should "save a plan" in {
      for {
        _ <- persistence.createPlan(key1, plan1)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1)
      }
    }

    it should "save a second plan" in {
      for {
        _ <- persistence.createPlan(key2, plan2)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1, key2)
      }
    }

    it should "save a third plan" in {
      for {
        _ <- persistence.createPlan(key3, plan3)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1, key2, key3)
      }
    }

    it should "throw any exception, when saving an already existing plan" in {
      recoverToSucceededIf[Exception] {
        persistence.createPlan(key2, plan3)
      }
    }

    it should "load the first, second and third plan in" in {
      for {
        p1 <- persistence.readPlan(key1)
        p2 <- persistence.readPlan(key2)
        p3 <- persistence.readPlan(key3)
      } yield {
        p1 shouldBe plan1
        p2 shouldBe plan2
        p3 shouldBe plan3
      }
    }

    it should "throw any exception, when loading a non-existent plan" in {
      recoverToSucceededIf[Exception] {
        persistence.readPlan(LevelKey("cat2", "lv2"))
      }
    }

    it should "remove the first plan" in {
      for {
        _ <- persistence.deletePlan(key1)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key2, key3)
      }
    }

    it should "throw any exception, when removing a non-existent plan" in {
      recoverToSucceededIf[Exception] {
        persistence.deletePlan(key1)
      }
    }

    it should "remove the second and third plan" in {
      for {
        _ <- persistence.deletePlan(key1)
        _ <- persistence.deletePlan(key2)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set.empty
      }
    }

  }

  "filePersistence" should behave like persistenceBehavior(
    new FilePersistenceModule("core/src/test/resources/plans")
  )

  "db4oPersistence" should behave like persistenceBehavior(
    new Db4oPersistenceModule("core/src/test/resources/scongo-test.db4o")
  )

  "slickH2Persistence" should behave like persistenceBehavior(
    new SlickH2PersistenceModule("scongo-test")
  )

  "mongoPersistence" should behave like persistenceBehavior(
    new MongoPersistenceModule("localhost:27017", "scongo-test")
  )

  "restPersistence" should behave like persistenceBehavior(

    new Module {
      binding to ActorSystem("scongo-rest-test")
      bind[Materializer] to ActorMaterializer()(inject[ActorSystem])
      bind[WSClient] to AhcWSClient()(inject[Materializer])
      bind[Persistence] to RestPersistence("http://localhost:9000")
    }
  )

}
