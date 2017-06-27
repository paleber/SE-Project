package persistence


class MongoPersistenceTest extends PersistenceBehavior {

  "mongoPersistence" should behave like persistenceBehavior(
    new MongoPersistence("localhost:27017", "scongo-test")
  )

}
