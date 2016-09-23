package gui

import java.awt._
import javax.swing.JPanel

import akka.actor.{Actor, ActorLogging}
import engine.{Grid, Point}
import model.Level

case class GuiGame(level: Level) extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")
  context.parent ! Gui.SetContentPane(this)


  val blocks = level.blocks.toArray
  val blockPolys = new Array[Polygon](blocks.length)

  for (i <- blocks.indices) {
    blockPolys(i) = new Polygon()
    for (j <- blocks(i).grids.indices) {
      blockPolys(i).addPoint(0, 0)
    }
  }

  val boardPoly = new Polygon()
  for (i <- 0 to level.board.corners.length) {
    boardPoly.addPoint(0, 0)
  }


  var scaleFactor: Double = 1
  var xOffset: Double = 0
  var yOffset: Double = 0

  override def paint(g: Graphics): Unit = {

    // Calculate scaleFactor and offsets
    scaleFactor = Math.min(getWidth / level.width, getHeight / level.height)
    xOffset = (getWidth - level.width * scaleFactor) / 2
    yOffset = (getHeight - level.height * scaleFactor) / 2

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
    drawGrid(g, level.board, Point(0, 0))

    // Draw the blocks
    for (block <- level.blocks) {
      drawGrid(g, block.grids(block.gridIndex), block.position)
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

    val poly = new java.awt.Polygon(xCoordinates, yCoordinates, grid.corners.length)


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
