package persistence


class Db4oPersistenceTest extends PersistenceBehavior {

  "db4oPersistence" should behave like persistenceBehavior(
    new Db4oPersistence("core/src/test/resources/scongo-test.db4o")
  )

}
