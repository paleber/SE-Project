package gui

import java.awt.{BasicStroke, Color, Graphics, Graphics2D, Polygon}
import javax.swing.JPanel

import akka.actor.{Actor, ActorLogging}
import engine.{Grid, Point}
import model.Level

case class GuiGame(level: Level) extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")

  val blocks = level.blocks.toArray
  val blockPolys = new Array[Polygon](blocks.length)

  for (i <- blocks.indices) {
    blockPolys(i) = new Polygon()
    for (j <- blocks(i).grids.head.corners.indices) {
      blockPolys(i).addPoint(0, 0)
    }
  }

  val boardPoly = new Polygon()
  for (i <- level.board.corners.indices) {
    boardPoly.addPoint(0, 0)
  }


  var scaleFactor: Double = 1
  var xOffset: Double = 0
  var yOffset: Double = 0



  context.parent ! Gui.SetContentPane(this)

  private def convertCornersToPoly(points: List[Point], position: Point, poly: Polygon): Unit = {
    for (i <- points.indices) {
      poly.xpoints(i) = scaleX(points(i).x + position.x)
      poly.ypoints(i) = scaleY(points(i).y + position.y)
    }
  }


  private val selectedBlock: Option[Int] = None

  override def paint(g: Graphics): Unit = {

    // Calculate scaleFactor and offsets
    scaleFactor = Math.min(getWidth / level.width, getHeight / level.height)
    xOffset = (getWidth - level.width * scaleFactor) / 2
    yOffset = (getHeight - level.height * scaleFactor) / 2



    convertCornersToPoly(level.board.corners, Point.ORIGIN, boardPoly)

    for(i <- blocks.indices) {
      convertCornersToPoly(blocks(i).activeGrid.corners, blocks(i).position, blockPolys(i))
    }

    // Draw the Background
    g.setColor(Color.GRAY)
    g.fillRect(0, 0, getWidth, getHeight)

    g.setColor(Color.WHITE)
    g.fillRect(scaleX(0), scaleY(0), scale(level.width), scale(level.height))

    g.setColor(Color.GRAY)
    for (y <- 1 until level.height.toInt) {
      g.drawLine(0, scaleY(y), getWidth, scaleY(y))
    }
    for (x <- 1 until level.width.toInt) {
      g.drawLine(scaleX(x), 0, scaleX(x), getHeight)
    }

    val g2 = g.asInstanceOf[Graphics2D]
    g2.setStroke(new BasicStroke((0.05 * scaleFactor).toInt))


    // Draw the board
    g.setColor(Color.LIGHT_GRAY)
    g.fillPolygon(boardPoly)

    g.setColor(Color.GRAY)
    g.drawPolygon(boardPoly)

    for (line <- level.board.lines) {
      g.drawLine(
        scaleX(line.start.x),
        scaleY(line.start.y),
        scaleX(line.end.x),
        scaleY(line.end.y))
    }


    // Draw the blocks
    for (i <- level.blocks.indices) {
      //drawGrid(g, block.grids(block.gridIndex), block.position)

      g.setColor(new Color(100, 255, 255))
      g.fillPolygon(blockPolys(i))

      g.setColor(new Color(0, 139, 139))
      g.drawPolygon(blockPolys(i))

      /*
      g.setColor(new Color(0, 153, 0))
      for (line <- blocks(i).grids(blocks(i).gridIndex).lines) {
        g.drawLine(
          scaleX(line.start.x + blocks(i).position.x),
          scaleY(line.start.y + blocks(i).position.y),
          scaleX(line.end.x + blocks(i).position.x),
          scaleY(line.end.y + blocks(i).position.y))
      }*/
      /*
      g.setColor(Color.RED)
      for (anchor <- grid.anchors) {
        g.fillOval(scaleX(anchor.x + position.x) - 2, scaleY(anchor.y + position.y) - 2, 4, 4)
      }
      g.setColor(Color.BLUE)
      g.drawPolygon(xCoordinates, yCoordinates, grid.corners.length)
*/
    }

  }

  private def drawGrid(g: Graphics, grid: Grid, position: Point): Unit = {


    val xCoordinates = new Array[Int](grid.corners.length)
    val yCoordinates = new Array[Int](grid.corners.length)
    for (i <- grid.corners.indices) {
      xCoordinates(i) = scaleX(grid.corners(i).x + position.x)
      yCoordinates(i) = scaleY(grid.corners(i).y + position.y)
    }

    g.setColor(new Color(200, 200, 255))
    g.fillPolygon(xCoordinates, yCoordinates, grid.corners.length)

    g.setColor(new Color(0, 153, 0))
    for (line <- grid.lines) {
      g.drawLine(
        scaleX(line.start.x + position.x),
        scaleY(line.start.y + position.y),
        scaleX(line.end.x + position.x),
        scaleY(line.end.y + position.y))
    }

    g.setColor(Color.RED)
    for (anchor <- grid.anchors) {
      g.fillOval(scaleX(anchor.x + position.x) - 2, scaleY(anchor.y + position.y) - 2, 4, 4)
    }


    g.setColor(Color.BLUE)
    g.drawPolygon(xCoordinates, yCoordinates, grid.corners.length)

  }

  private def scale(z: Double): Int = {
    (z * scaleFactor).toInt
  }

  private def scaleX(z: Double): Int = {
    (z * scaleFactor + xOffset).toInt
  }

  private def scaleY(z: Double): Int = {
    (z * scaleFactor + yOffset).toInt
  }


  override def receive = {
    case msg => log.warning("Unhandled message: " + msg)
  }

}
