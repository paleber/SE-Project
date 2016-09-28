package gui

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.{BasicStroke, Color, Graphics, Graphics2D, Polygon}
import javax.swing.JPanel

import akka.actor.{Actor, ActorLogging}
import engine.{Grid, Point}
import model.{Block, Level}
import msg.{ClientMessage, ServerMessage}


case class GuiGame(level: Level) extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")

  var blocks = level.blocks.toArray
  var blockPolys = Array.fill[Polygon](blocks.length)(new Polygon())

  val boardPoly = new Polygon()

  var scaleFactor: Double = 1
  var xOffset: Double = 0
  var yOffset: Double = 0

  private var lastX: Int = 0
  private var lastY: Int = 0

  var mouseX = 0
  var mouseY = 0

  private class Selected(var index: Int, var block: Block) {
    var poly = new Polygon()
  }

  private var selected: Option[Selected] = None


  addMouseMotionListener(new MouseAdapter {
    override def mouseDragged(e: MouseEvent): Unit = {
      if (selected.isDefined) {
        val delta = Point((e.getX - lastX) / scaleFactor, (e.getY - lastY) / scaleFactor)
        selected.get.block = selected.get.block.copy(
          position = selected.get.block.position + delta
        )
        lastX = e.getX
        lastY = e.getY
      }
    }
  })

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      selected = None
      for (poly <- blockPolys) {
        if (poly.contains(e.getPoint)) {
          lastX = e.getX
          lastY = e.getY
          val index = blockPolys.indexOf(poly)
          selected = Some(new Selected(index, blocks(index)))
        }
      }
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      if (selected.isDefined) {
        val msg = ClientMessage.UpdateBlockPosition(
          selected.get.index,
          Point(e.getX / scaleFactor - xOffset, e.getY / scaleFactor - yOffset)
        )


        blocks(selected.get.index) = selected.get.block
        selected = None
        context.parent ! msg
      }
    }
  })

  context.parent ! Gui.SetContentPane(this)

  private def convertCornersToPoly(points: List[Point], position: Point, poly: Polygon): Unit = {
    poly.reset()
    for (i <- points.indices) {
      poly.addPoint(
        scaleX(points(i).x + position.x),
        scaleY(points(i).y + position.y)
      )
    }
  }


  override def paint(g: Graphics): Unit = {

    // Calculate scaleFactor and offsets
    scaleFactor = Math.min(getWidth / level.width, getHeight / level.height)
    xOffset = (getWidth - level.width * scaleFactor) / 2
    yOffset = (getHeight - level.height * scaleFactor) / 2

    // Convert corners to polygon
    convertCornersToPoly(level.board.corners, Point.ORIGIN, boardPoly)
    for (i <- blocks.indices) {
      convertCornersToPoly(blocks(i).grid.corners, blocks(i).position, blockPolys(i))
    }

    /*/ Select the block
    selectedBlockIndex = None
    blocks.foreach(b => {
      if (b.poly.contains(mouseX, mouseY)) {
        selectedBlockIndex = Some(b)
      }
    })*/

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


    // Draw unselected the blocks
    for (poly <- blockPolys if selected.isEmpty ||
      blockPolys.indexOf(poly) != selected.get.index) {

      g.setColor(new Color(100, 255, 255))
      g.fillPolygon(poly)

      g.setColor(new Color(0, 139, 139))
      g.drawPolygon(poly)
    }

    if (selected.isDefined) {
      convertCornersToPoly(
        selected.get.block.grid.corners,
        selected.get.block.position,
        selected.get.poly
      )

      g.setColor(new Color(100, 255, 100))
      g.fillPolygon(selected.get.poly)

      g.setColor(new Color(0, 139, 0))
      g.drawPolygon(selected.get.poly)
    }

  }

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
    case ServerMessage.UpdateBlock(index, block) =>
      blocks(index) = block

    case msg => log.warning("Unhandled message: " + msg)
  }

}
