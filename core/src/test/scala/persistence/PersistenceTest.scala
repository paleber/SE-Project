package persistence

import model.element.{LevelId, Plan}
import org.scalatest.{FlatSpec, Matchers}
import scaldi.{Injectable, Injector}

class PersistenceTest extends FlatSpec with Matchers with Injectable {

  private val id1 = LevelId("cat1", "lv1")
  private val plan1 = Plan(4, List(List(List())))

  private val id2 = LevelId("cat1", "lv2")
  private val plan2 = Plan(4, List(List(List(1, 2), List(3, 2)), List(List(1, 2, 2)), List(List(2, 3))))

  private val id3 = LevelId("cat2", "lv1")
  private val plan3 = Plan(6, List(List(List(1))))

  def persistenceBehavior(implicit injector: Injector): Unit = {

    val persistence = inject[Persistence]

    it should "remove all already existing plans" in {
      persistence.loadIds foreach persistence.removePlan
      persistence.loadIds shouldBe List.empty
    }

    it should "save a plan" in {
      persistence.savePlan(id1, plan1)
      persistence.loadIds shouldBe List(id1)
    }

    it should "save a second plan" in {
      persistence.savePlan(id2, plan2)
      val ids = persistence.loadIds
      ids.size shouldBe 2
      ids should contain(id1)
      ids should contain(id2)
    }

    it should "save a third plan" in {
      persistence.savePlan(id3, plan3)
      val ids = persistence.loadIds
      ids.size shouldBe 3
      ids should contain(id1)
      ids should contain(id2)
      ids should contain(id3)
    }

    it should "throw any exception, when saving an already existing plan" in {
      intercept[Exception] {
        persistence.savePlan(id2, plan3)
      }
    }

    it should "load the first, second and third plan in" in {
      persistence.loadPlan(id1) shouldBe plan1
      persistence.loadPlan(id2) shouldBe plan2
      persistence.loadPlan(id3) shouldBe plan3
    }

    it should "throw any exception, when loading a non-existent plan" in {
      intercept[Exception] {
        persistence.loadPlan(LevelId("cat2", "lv2"))
      }
    }

    it should "remove the first plan" in {
      persistence.removePlan(id1)
      val ids = persistence.loadIds
      ids.size shouldBe 2
      ids should not contain id1
      ids should contain(id2)
      ids should contain(id3)
    }

    it should "throw any exception, when removing a non-existent plan" in {
      intercept[Exception] {
        persistence.removePlan(id1)
      }
    }

    it should "remove the second and third plan" in {
      persistence.removePlan(id2)
      persistence.removePlan(id3)
      persistence.loadIds shouldBe List.empty
    }

  }

  "filePersistence" should behave like persistenceBehavior(
    FilePersistenceModule("core/src/test/resources/plans")
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

}
