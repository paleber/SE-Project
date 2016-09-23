package util

/**
  * Created by Patrick on 23.09.2016.
  */
object IdGenerator {

  private var idCounter = 0

  def generate(): Int = {
    idCounter += 1
    idCounter
  }

}
