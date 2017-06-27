package persistence


class FilePersistenceTest extends PersistenceBehavior {

  "filePersistence" should behave like persistenceBehavior(
    new FilePersistence("core/src/test/resources/plans")
  )

}
