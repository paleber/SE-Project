package loader

import model.{Block, Grid, Level, Point}

import scala.collection.mutable.ListBuffer

object LevelLoader {

  val rotationSteps = 4

  def load(levelIndex: Int): Option[Level] = {

    levelIndex match {

      case 0 =>
        val board = GridLoader.load(3) + Point(4.5, 2)
        Some(Level(9, 7, board,
          List(
            Block(GridLoader.load(0), Point(2.5, 4.5)),
            Block(GridLoader.load(1), Point(4.5, 4.5)),
            Block(GridLoader.load(2), Point(7, 4.5))
          ), 4, buildAnchors(4, board, 9, 7).toList
        ))

      case 1 =>
        val board = GridLoader.load(1000) + Point(6, 4.5)
        Some(Level(12, 9, board,
          List(
            Block(GridLoader.load(1001), Point(6.5, 5.5)),
            Block(GridLoader.load(1002), Point(3.5, 5.5))
          ), 6, buildAnchors(6, board, 12, 9).toList
        )
        )


      case _ => None
    }

  }

  def createLevel(plan: LevelPlan): Level = {
    val mid = Point(plan.width / 2, plan.height / 2)

    val anchors = buildAnchors(plan.rotationSteps, null, plan.width, plan.height)



    null
  }


  def minAnchorDistanceSquare(rotationSteps: Int): Double = {
    assert(rotationSteps == 4 || rotationSteps == 6)
    rotationSteps match {
      case 4 => Math.pow(1.49, 2)
      case 6 => Math.pow(1.8, 2)
    }
  }

  private def buildAnchors(rotationSteps: Int, board: Grid, width: Double, height: Double): ListBuffer[Point] = {
    val dirs = GridLoader.buildDirections(rotationSteps).toArray.transform(v => v * 0.5).toList
    val anchors = ListBuffer(board.anchors.head)
    var index = 0
    while (index < anchors.length) {
      dirs.foreach(v => addAnchor(anchors(index) + v, anchors, width, height))
      index += 1
    }


    val minDistanceSquare = minAnchorDistanceSquare(rotationSteps)

    board.anchors.foreach(boardAnchor => {
      anchors.foreach(freeAnchor => {
        if (freeAnchor.distanceSquareTo(boardAnchor) < minDistanceSquare) {
          anchors -= freeAnchor
        }
      })
    })

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


