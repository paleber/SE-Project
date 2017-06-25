package persistence

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import model.element.{LevelKey, Plan}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import scaldi.{Injectable, Injector, Module}

class PersistenceTest extends FlatSpec with Matchers with Injectable {

  private val id1 = LevelKey("cat1", "lv1")
  private val plan1 = Plan(4, List(List(List())))

  private val id2 = LevelKey("cat1", "lv2")
  private val plan2 = Plan(4, List(List(List(1, 2), List(3, 2)), List(List(1, 2, 2)), List(List(2, 3))))

  private val id3 = LevelKey("cat2", "lv1")
  private val plan3 = Plan(6, List(List(List(1))))

  def persistenceBehavior(implicit injector: Injector): Unit = {

    val persistence = inject[Persistence]

    it should "remove all already existing plans" in {
      persistence.readAllKeys foreach persistence.deletePlan
      persistence.readAllKeys shouldBe List.empty
    }

    it should "save a plan" in {
      persistence.createPlan(id1, plan1)
      persistence.readAllKeys shouldBe List(id1)
    }

    it should "save a second plan" in {
      persistence.createPlan(id2, plan2)
      val ids = persistence.readAllKeys
      ids.size shouldBe 2
      ids should contain(id1)
      ids should contain(id2)
    }

    it should "save a third plan" in {
      persistence.createPlan(id3, plan3)
      val ids = persistence.readAllKeys
      ids.size shouldBe 3
      ids should contain(id1)
      ids should contain(id2)
      ids should contain(id3)
    }

    it should "throw any exception, when saving an already existing plan" in {
      intercept[Exception] {
        persistence.createPlan(id2, plan3)
      }
    }

    it should "load the first, second and third plan in" in {
      persistence.readPlan(id1) shouldBe plan1
      persistence.readPlan(id2) shouldBe plan2
      persistence.readPlan(id3) shouldBe plan3
    }

    it should "throw any exception, when loading a non-existent plan" in {
      intercept[Exception] {
        persistence.readPlan(LevelKey("cat2", "lv2"))
      }
    }

    it should "remove the first plan" in {
      persistence.deletePlan(id1)
      val ids = persistence.readAllKeys
      ids.size shouldBe 2
      ids should not contain id1
      ids should contain(id2)
      ids should contain(id3)
    }

    it should "throw any exception, when removing a non-existent plan" in {
      intercept[Exception] {
        persistence.deletePlan(id1)
      }
    }

    it should "remove the second and third plan" in {
      persistence.deletePlan(id2)
      persistence.deletePlan(id3)
      persistence.readAllKeys shouldBe List.empty
    }

  }

  "filePersistence" should behave like persistenceBehavior(
    new Module {bind[Persistence] to FilePersistence("core/src/test/resources/plans")}
  )

  "db4oPersistence" should behave like persistenceBehavior(
    Db4oPersistenceModule("core/src/test/resources/scongo-test.db4o")
  )

  "slickH2Persistence" should behave like persistenceBehavior(
    SlickH2PersistenceModule("scongo-test")
  )

  "mongoPersistence" should behave like persistenceBehavior(
    MongoPersistenceModule("localhost:27017", "scongo-test")
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
