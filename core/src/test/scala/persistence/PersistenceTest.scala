package persistence

import model.element.{LevelId, Plan}
import org.scalatest.{FlatSpec, Matchers}
import scaldi.{Injectable, Module}

import scala.collection.mutable

/*
trait PersistenceBehavior {
  this: FlatSpec with Matchers =>

  def persistenceBehavior(persistence: => Persistence): Unit = {

    it should "first load empty MetaInfo" in {
      val metaInfo = persistence.loadMetaInfo
    }

    it should behave like()
  }
}*/

class PersistenceTest extends FlatSpec with Matchers with Injectable {


  def persistenceBehavior(persistence: Persistence): Unit = {

    it should "first load empty MetaInfo" in {
      val metaInfo = persistence.loadMetaInfo
    }

  }


  "db4oPersistence" should behave like persistenceBehavior({
    implicit object Injector extends Module {
      bind[Persistence] toProvider new Db4oPersistence
      bind[String] identifiedBy 'db4oDatabaseName to "testDb4oDatabase"
    }
    inject[Persistence]
  })


  /*
    private val plan1 = Plan(4, List(List(List())))
    private val id1 = LevelId("cat1", "lv1")

    persistence.savePlan(id1, plan1)
  */

}
