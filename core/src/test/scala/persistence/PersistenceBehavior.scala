package persistence

import scala.concurrent.Future

import model.element.LevelKey
import model.element.Plan
import org.scalatest.AsyncFlatSpec
import org.scalatest.Matchers
import persistence.PersistenceBehavior.key1
import persistence.PersistenceBehavior.key2
import persistence.PersistenceBehavior.key3
import persistence.PersistenceBehavior.plan1
import persistence.PersistenceBehavior.plan2
import persistence.PersistenceBehavior.plan3


object PersistenceBehavior {

  private val key1 = LevelKey("cat1", "lv1")
  private val plan1 = Plan(4, List(List(List())))

  private val key2 = LevelKey("cat1", "lv2")
  private val plan2 = Plan(4, List(List(List(1, 2), List(3, 2)), List(List(1, 2, 2)), List(List(2, 3))))

  private val key3 = LevelKey("cat2", "lv1")
  private val plan3 = Plan(6, List(List(List(1))))

}

trait PersistenceBehavior extends AsyncFlatSpec with Matchers {

  def persistenceBehavior(persistence: Persistence): Unit = {

    it should "delete all already existing plans" in {
      for {
        existingKeys <- persistence.readAllKeys()
        _ <- Future.sequence(existingKeys.map(persistence.deletePlan))
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set.empty
      }
    }

    it should "create a plan" in {
      for {
        _ <- persistence.createPlan(key1, plan1)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1)
      }
    }

    it should "create a second plan" in {
      for {
        _ <- persistence.createPlan(key2, plan2)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1, key2)
      }
    }

    it should "create a third plan" in {
      for {
        _ <- persistence.createPlan(key3, plan3)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1, key2, key3)
      }
    }

    it should "throw any exception, when creating an already existing plan" in {
      recoverToSucceededIf[Exception] {
        persistence.createPlan(key2, plan3)
      }
    }

    it should "read the first, second and third plan in" in {
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

    it should "throw any exception, when reading a non-existent plan" in {
      recoverToSucceededIf[Exception] {
        persistence.readPlan(LevelKey("cat2", "lv2"))
      }
    }

    it should "delete the first plan" in {
      for {
        _ <- persistence.deletePlan(key1)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key2, key3)
      }
    }

    it should "throw any exception, when deleting a non-existent plan" in {
      recoverToSucceededIf[Exception] {
        persistence.deletePlan(key1)
      }
    }

    it should "delete the second and third plan" in {
      for {
        _ <- persistence.deletePlan(key2)
        _ <- persistence.deletePlan(key3)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set.empty
      }
    }

    it should "create all three plans again" in {
      for {
        _ <- persistence.createPlan(key1, plan1)
        _ <- persistence.createPlan(key2, plan2)
        _ <- persistence.createPlan(key3, plan3)
        keys <- persistence.readAllKeys()
      } yield {
        keys shouldBe Set(key1, key2, key3)
      }
    }

  }

}
