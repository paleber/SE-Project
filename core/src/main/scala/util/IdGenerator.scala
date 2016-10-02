package util


object IdGenerator {

  private var idCounter = 0

  def generate(): Int = {
    idCounter += 1
    idCounter
  }

}
