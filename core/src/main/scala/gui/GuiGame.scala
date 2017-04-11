package gui

import java.awt.event.{KeyAdapter, KeyEvent, MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Color, Cursor, Font, Graphics, Graphics2D, Polygon, Toolkit, Point => AwtPoint}
import javax.swing.JPanel

import akka.actor.{Actor, ActorLogging, Props}
import builder.Game._
import model.basic.{Line, Point}
import model.element.{Grid, Level}
import persistence.ResourceManager.{LevelLoaded, LoadMenu}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object GuiGame {

  def props: Props = Props[GuiGame]

}

private class GuiGame extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")

  private var level: Level = _

  private var blocks: Array[Grid] = Array.empty

  private var scaleFactor: Double = 1
  private var xOffset, yOffset: Double = 0
  private var lastX, lastY: Int = 0

  private var finished: Option[Double] = None

  private val defaultCursor = Cursor.getDefaultCursor

  private val blankCursor = Toolkit.getDefaultToolkit.createCustomCursor(
    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
    new AwtPoint(0, 0),
    null
  )

  private class Selected(var index: Int, var block: Grid)

  private var selected: Option[Selected] = None

  private case class MoveBlock(p: AwtPoint)

  private case class SelectBlock(p: AwtPoint)

  private case object ReleaseBlock

  private sealed trait Action

  private case object RotateRight extends Action

  private case object RotateLeft extends Action

  private case object MirrorVertical extends Action

  private case object MirrorHorizontal extends Action

  private case object BackToMenu

  private case object BackToMenuWhenFinished

  private var activeAction: Option[BlockAction] = None

  private case class BlockAction(action: Action, startGrid: Grid, curStep: Int = 0, maxSteps: Int = 6)

  private case object HandleBlockAction


  addMouseMotionListener(new MouseAdapter {
    override def mouseDragged(e: MouseEvent): Unit = self ! MoveBlock(e.getPoint)
  })

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = self ! SelectBlock(e.getPoint)

    override def mouseReleased(e: MouseEvent): Unit = self ! ReleaseBlock
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = e.getKeyCode match {
      case KeyEvent.VK_A => self ! RotateLeft
      case KeyEvent.VK_LEFT => self ! RotateLeft

      case KeyEvent.VK_D => self ! RotateRight
      case KeyEvent.VK_RIGHT => self ! RotateRight

      case KeyEvent.VK_W => self ! MirrorVertical
      case KeyEvent.VK_UP => self ! MirrorVertical

      case KeyEvent.VK_S => self ! MirrorHorizontal
      case KeyEvent.VK_DOWN => self ! MirrorHorizontal

      case KeyEvent.VK_ESCAPE => self ! BackToMenu

      case KeyEvent.VK_ENTER => self ! BackToMenuWhenFinished
      case KeyEvent.VK_SPACE => self ! BackToMenuWhenFinished

      case _ => log.warning(s"Ignoring Key: ${e.getKeyCode} - ${e.getKeyChar}")
    }
  })

  private def createPoly(points: List[Point], position: Point): Polygon = {
    val poly = new Polygon()
    points.foreach(p => poly.addPoint(
      scaleX(p.x + position.x),
      scaleY(p.y + position.y)
    ))
    poly
  }

  private def createEdge(points: List[Point], position: Point): Polygon = {
    val poly = new Polygon()
    points.foreach(p => poly.addPoint(
      scaleX(p.x + position.x),
      scaleY(p.y + position.y)
    ))
    poly
  }

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
    for (y <- 1 to level.height.toInt) {
      g.drawLine(0, scaleY(y), getWidth, scaleY(y))
    }
    for (x <- 1 to level.width.toInt) {
      g.drawLine(scaleX(x), 0, scaleX(x), getHeight)
    }

    val g2 = g.asInstanceOf[Graphics2D]
    g2.setStroke(new BasicStroke((0.05 * scaleFactor).toInt))

    // Convert corners to polygon
    level.board.absolute.polygons.foreach(p => {

      val boardPoly = createPoly(p, Point.ZERO)

      // Draw the board
      g.setColor(Color.LIGHT_GRAY)
      g.fillPolygon(boardPoly)

      g.setColor(Color.GRAY)
      g.drawPolygon(boardPoly)

      for (line <- level.board.absolute.edges) {
        g.drawLine(
          scaleX(line.start.x),
          scaleY(line.start.y),
          scaleX(line.end.x),
          scaleY(line.end.y))
      }
    })

    // Draw unselected the blocks
    blocks.foreach(block => {
      if (selected.isEmpty || blocks.indexOf(block) != selected.get.index) {
        g.setColor(new Color(100, 255, 255))
        block.polygons.foreach(p => {
          val poly = createPoly(p, block.position)
          g.fillPolygon(poly)
        })
        g.setColor(new Color(0, 139, 139))
        block.edges.foreach(edge => drawEdge(g, edge, block.position))
      }
    })

    if (selected.isDefined) {
      g.setColor(new Color(100, 255, 100))
      selected.get.block.polygons.foreach(p => {
        val poly = createPoly(p, selected.get.block.position)
        g.fillPolygon(poly)
      })

      g.setColor(new Color(0, 139, 0))
      selected.get.block.edges.foreach(edge => drawEdge(g, edge, selected.get.block.position))
    }

    if (finished.isDefined) {
      g.setColor(Color.BLUE)
      g.setFont(new Font("Arial Black", Font.BOLD, (0.7 * scaleFactor).toInt))

      val sFinish = "LEVEL COMPLETED"
      g.drawString(
        sFinish,
        (getWidth - g.getFontMetrics.stringWidth(sFinish)) / 2,
        (getHeight - 6 * g.getFontMetrics.getHeight) / 2
      )

      g.setFont(new Font("Arial Black", Font.BOLD, (0.5 * scaleFactor).toInt))
      val sTime = s"${finished.get} SECONDS"
      g.drawString(
        sTime,
        (getWidth - g.getFontMetrics.stringWidth(sTime)) / 2,
        (getHeight - 6 * g.getFontMetrics.getHeight) / 2
      )

      g.setFont(new Font("Arial Black", Font.BOLD, (0.4 * scaleFactor).toInt))
      val sEnter = s"PRESS ENTER"
      g.drawString(
        sEnter,
        (getWidth - g.getFontMetrics.stringWidth(sEnter)) / 2,
        getHeight / 2
      )
    }
  }

  private def drawEdge(g: Graphics, edge: Line, position: Point): Unit = {
    g.drawLine(
      scaleX(edge.start.x + position.x),
      scaleY(edge.start.y + position.y),
      scaleX(edge.end.x + position.x),
      scaleY(edge.end.y + position.y)
    )
  }

  private def scale(z: Double): Int = {
    (z * scaleFactor + 0.5).toInt
  }

  private def scaleX(z: Double): Int = {
    (z * scaleFactor + xOffset + 0.5).toInt
  }

  private def scaleY(z: Double): Int = {
    (z * scaleFactor + yOffset + 0.5).toInt
  }

  override def receive: Receive = {

    case LevelLoaded(lv) =>
      level = lv
      blocks = level.blocks.toArray
      finished = None
      context.parent ! Gui.SetContentPane(this, "game - " + level.id.category + " " + level.id.name)

    case BlockUpdated(index, block) =>
      blocks(index) = block

    case LevelFinished(timeMillis) =>
      finished = Some((timeMillis / 100).toDouble / 10)

    case MoveBlock(position) =>
      if (selected.isDefined) {
        val delta = Point((position.x - lastX) / scaleFactor, (position.y - lastY) / scaleFactor)
        selected.get.block = selected.get.block.copy(
          position = selected.get.block.position + delta
        )
        lastX = position.x
        lastY = position.y
      }

    case SelectBlock(position) =>
      if (finished.isEmpty) {
        selected = None

        blocks.foreach(block => {
          block.polygons.foreach(p => {
            val poly = createPoly(p, block.position)
            if (poly.contains(position)) {
              lastX = position.x
              lastY = position.y
              val index = blocks.indexOf(block)
              selected = Some(new Selected(index, block))
              setCursor(blankCursor)
            }
          })
        })
      }

    case ReleaseBlock =>
      if (selected.isDefined) {
        val msg = UpdateBlockPosition(
          selected.get.index,
          selected.get.block.position
        )
        blocks(selected.get.index) = selected.get.block
        selected = None
        activeAction = None
        setCursor(defaultCursor)
        context.parent ! msg
      }

    case RotateLeft =>
      if (selected.isDefined && activeAction.isEmpty) {
        context.parent ! RotateBlockLeft(selected.get.index)
        activeAction = Some(BlockAction(RotateLeft, selected.get.block))
        self ! HandleBlockAction
      }

    case RotateRight =>
      if (selected.isDefined && activeAction.isEmpty) {
        context.parent ! RotateBlockRight(selected.get.index)
        activeAction = Some(BlockAction(RotateRight, selected.get.block))
        self ! HandleBlockAction
      }

    case MirrorVertical =>
      if (selected.isDefined && activeAction.isEmpty) {
        context.parent ! MirrorBlockVertical(selected.get.index)
        activeAction = Some(BlockAction(MirrorVertical, selected.get.block))
        self ! HandleBlockAction
      }

    case MirrorHorizontal =>
      if (selected.isDefined && activeAction.isEmpty) {
        context.parent ! MirrorBlockHorizontal(selected.get.index)
        activeAction = Some(BlockAction(MirrorHorizontal, selected.get.block))
        self ! HandleBlockAction
      }

    case BackToMenu =>
      context.parent ! LoadMenu

    case BackToMenuWhenFinished =>
      if (finished.isDefined) {
        context.parent ! LoadMenu
      }

    case HandleBlockAction =>
      handleBlockAction()

  }

  private def handleBlockAction(): Unit = {
    if (activeAction.isDefined) {
      activeAction.get.action match {

        case RotateLeft =>
          selected.get.block = activeAction.get.startGrid.rotate(
            -Math.PI * 2 / level.form / activeAction.get.maxSteps * activeAction.get.curStep
          )

        case RotateRight =>
          selected.get.block = activeAction.get.startGrid.rotate(
            Math.PI * 2 / level.form / activeAction.get.maxSteps * activeAction.get.curStep
          )

        case MirrorVertical =>
          selected.get.block = activeAction.get.startGrid.mirrorVertical(
            activeAction.get.curStep.toFloat / activeAction.get.maxSteps
          )

        case MirrorHorizontal =>
          selected.get.block = activeAction.get.startGrid.mirrorHorizontal(
            activeAction.get.curStep.toFloat / activeAction.get.maxSteps
          )

        case msg => unhandled(msg)
      }

      if (activeAction.get.curStep < activeAction.get.maxSteps) {
        activeAction = Some(activeAction.get.copy(
          curStep = activeAction.get.curStep + 1)
        )
        context.system.scheduler.scheduleOnce(20 millis) {
          self ! HandleBlockAction
        }
      } else {
        activeAction = None
      }
    }
  }
}
