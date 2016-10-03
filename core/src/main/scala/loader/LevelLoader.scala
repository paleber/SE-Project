package loader

import model.{Block, Level, Point}

import scala.collection.mutable.ListBuffer

object LevelLoader {

  val rotationSteps = 4

  def load(levelIndex: Int): Option[Level] = {

    levelIndex match {

      case 0 =>
        Some(Level(9, 7, GridLoader.load(3) + Point(4.5, 2),
          List(
            Block(GridLoader.load(0), Point(2.5, 4.5)),
            Block(GridLoader.load(1), Point(4.5, 4.5)),
            Block(GridLoader.load(2), Point(7, 4.5))
          ), 4, buildAnchors(4, GridLoader.load(3).anchors.head + Point(4.5, 2), 9, 7).toList
        ))

      case 1 =>
        Some(Level(12, 9, GridLoader.load(1000) + Point(6, 4.5),
          List(
            Block(GridLoader.load(1001), Point(6.5, 5.5)),
            Block(GridLoader.load(1002), Point(3.5, 5.5))
          ), 6, buildAnchors(6, GridLoader.load(1000).anchors.head + Point(6, 4.5), 12, 9).toList
        )


        )


      case _ => None
    }

  }

  def createLevel(plan: LevelPlan): Level = {
    val mid = Point(plan.width / 2, plan.height / 2)

    val anchors = buildAnchors(plan.rotationSteps, Point.ORIGIN, plan.width, plan.height)



    null
  }

  private def buildAnchors(rotationSteps: Int, start: Point, width: Double, height: Double): ListBuffer[Point] = {
    val dirs = GridLoader.buildDirections(rotationSteps).toArray.transform(v => v * 0.5).toList
    val anchors = ListBuffer(start)
    var index = 0
    while (index < anchors.length) {
      dirs.foreach(v => addAnchor(anchors(index) + v, anchors, width, height))
      index += 1
    }
    anchors
  }

  private def addAnchor(p: Point, anchors: ListBuffer[Point], width: Double, height: Double): Unit = {
    if (p.x < 0.99 || p.x > width - 0.99) {
      return
    }
    if (p.y < 0.99 || p.y > height - 0.99) {
      return
    }
    anchors.foreach(a =>
      if (a.distanceSquareTo(p) < 1e-5) {
        return
      }
    )
    anchors += p
  }

}


case class LevelPlan(rotationSteps: Int,
                     width: Int,
                     height: Int,
                     boardId: Int,
                     blockIds: List[Int])



