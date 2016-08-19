package gui

import java.awt.{Color, Container, Graphics}
import javax.swing.{JFrame, JPanel}

import loader.LevelLoader
import model.Level


case class ContentFrame(content: Container) {

  val frame = new JFrame

  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  frame.setSize(800, 600)
  frame.setLocationRelativeTo(null)
  frame.setVisible(true)

  frame.setContentPane(content)

}

case class LevelPanel(level: Level) extends JPanel {

  var scaleFactor: Double = 1
  var xOffset: Double = 0
  var yOffset: Double = 0

  override def paint(g: Graphics): Unit = {

    scaleFactor = Math.min(getWidth / level.width, getHeight / level.height)

    xOffset = (getWidth - level.width * scaleFactor) / 2
    yOffset = (getHeight - level.height * scaleFactor) / 2

    g.setColor(Color.DARK_GRAY)
    g.fillRect(0, 0, getWidth, getHeight)

    g.setColor(Color.WHITE)
    g.fillRect(scaleX(0), scaleY(0), scale(level.width), scale(level.height))

    val xCoordinates = new Array[Int](level.grid.corners.length)
    val yCoordinates = new Array[Int](level.grid.corners.length)
    for (i <- level.grid.corners.indices) {
      xCoordinates(i) = scaleX(level.grid.corners(i).x + level.gridPosition.x)
      yCoordinates(i) = scaleY(level.grid.corners(i).y + level.gridPosition.y)
    }

    g.setColor(new Color(200, 200, 255))
    g.fillPolygon(xCoordinates, yCoordinates, level.grid.corners.length)


    g.setColor(Color.DARK_GRAY)
    g.drawLine(0, getHeight / 2, getWidth, getHeight / 2)
    g.drawLine(getWidth / 2, 0, getWidth / 2, getHeight)

    g.setColor(new Color(0, 153, 0))
    for (line <- level.grid.lines) {
      g.drawLine(
        scaleX(line.start.x + level.gridPosition.x),
        scaleY(line.start.y + level.gridPosition.y),
        scaleX(line.end.x + level.gridPosition.x),
        scaleY(line.end.y + level.gridPosition.y))
    }

    g.setColor(Color.BLUE)
    g.drawPolygon(xCoordinates, yCoordinates, level.grid.corners.length)



    g.setColor(Color.RED)
    for (anchor <- level.grid.anchors) {
      g.fillOval(scaleX(anchor.x + level.gridPosition.x) - 2, scaleY(anchor.y + level.gridPosition.y) - 2, 4, 4)
    }
  }

  def scale(z: Double): Int = {
    (z * scaleFactor).toInt
  }

  def scaleX(z: Double): Int = {
    (z * scaleFactor + xOffset).toInt
  }

  def scaleY(z: Double): Int = {
    (z * scaleFactor + yOffset).toInt
  }

}

object Starter extends App {

  val levelPanel = LevelPanel(LevelLoader.load)
  ContentFrame(levelPanel)

}