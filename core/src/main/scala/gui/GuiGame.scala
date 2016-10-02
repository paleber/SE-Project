package gui

import java.awt.event.{KeyAdapter, KeyEvent, MouseAdapter, MouseEvent}
import java.awt.{BasicStroke, Color, Graphics, Graphics2D, Polygon}
import javax.swing.JPanel
import java.awt.{Point => AwtPoint}

import akka.actor.{Actor, ActorLogging}
import engine.Point
import model.{Block, Level}
import msg.{ClientMessage, ServerMessage}


case class GuiGame(level: Level) extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")

  private val blocks = level.blocks.toArray
  private val blockPolys = Array.fill[Polygon](blocks.length)(new Polygon())

  private val boardPoly = new Polygon()

  private var scaleFactor: Double = 1
  private var xOffset, yOffset: Double = 0
  private var lastX, lastY: Int = 0


  private class Selected(var index: Int, var block: Block) {
    var poly = new Polygon()
  }

  private var selected: Option[Selected] = None


  private case class MoveBlock(p: AwtPoint)

  private case class SelectBlock(p: AwtPoint)

  private case object ReleaseBlock

  private case object RotateRight

  private case object RotateLeft

  private case object MirrorVertical

  private case object MirrorHorizontal

  private case object BackToMenu

  addMouseMotionListener(new MouseAdapter {
    override def mouseDragged(e: MouseEvent) = self ! MoveBlock(e.getPoint)
  })

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) = self ! SelectBlock(e.getPoint)

    override def mouseReleased(e: MouseEvent) = self ! ReleaseBlock
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) = e.getKeyCode match {
      case KeyEvent.VK_A => self ! RotateLeft
      case KeyEvent.VK_LEFT => self ! RotateLeft

      case KeyEvent.VK_D => self ! RotateRight
      case KeyEvent.VK_RIGHT => self ! RotateRight

      case KeyEvent.VK_W => self ! MirrorVertical
      case KeyEvent.VK_UP => self ! MirrorVertical

      case KeyEvent.VK_S => self ! MirrorHorizontal
      case KeyEvent.VK_DOWN => self ! MirrorHorizontal

      case KeyEvent.VK_ESCAPE => self ! BackToMenu

      case msg => log.warning(s"Ignoring Key: ${e.getKeyCode} - ${e.getKeyChar}")
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

    case MoveBlock(point) =>
      if (selected.isDefined) {
        val delta = Point((point.x - lastX) / scaleFactor, (point.y - lastY) / scaleFactor)
        selected.get.block = selected.get.block.copy(
          position = selected.get.block.position + delta
        )
        lastX = point.x
        lastY = point.y
      }

    case SelectBlock(point) =>
      selected = None
      for (poly <- blockPolys) {
        if (poly.contains(point)) {
          lastX = point.x
          lastY = point.y
          val index = blockPolys.indexOf(poly)
          selected = Some(new Selected(index, blocks(index)))
        }
      }

    case ReleaseBlock =>
      if (selected.isDefined) {
        val msg = ClientMessage.UpdateBlockPosition(
          selected.get.index,
          selected.get.block.position
        )
        blocks(selected.get.index) = selected.get.block
        selected = None
        context.parent ! msg
      }


    case RotateLeft => println("Left")
    case RotateRight => println("Right")
    case MirrorVertical => println("Vertical")
    case MirrorHorizontal => println("Horizontal")


    case BackToMenu => context.parent ! ClientMessage.ShowMenu

    case msg => log.warning("Unhandled message: " + msg)
  }

}
