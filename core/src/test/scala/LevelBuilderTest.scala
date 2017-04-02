import builder.LevelBuilder
import model.element.Plan
import org.scalatest._


class LevelBuilderTest extends FlatSpec with Matchers {

  "LevelBuilder" should "build a 1x1 Level" in {
    val plan = Plan(4, List(List(List())))
    val level = LevelBuilder.build(null, plan)
    println(level)
    // TODO verify
  }


}
