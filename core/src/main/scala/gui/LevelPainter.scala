package gui

import java.awt.{Color, Container, Graphics}
import javax.swing.{JFrame, JPanel}

import loader.LevelLoader
import model.Level


case class ContentFrame(content: Container) {

  val frame = new JFrame

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

    // TODO Calculate Offset

    g.setColor(Color.BLUE)

    for (line <- level.grid.border) {
      g.drawLine(
        scale(line.start.x + level.gridPosition.x),
        scale(line.start.y+ level.gridPosition.y),
        scale(line.end.x+ level.gridPosition.x),
        scale(line.end.y+ level.gridPosition.y))
    }

    //g.fillRect(0,0,100,200)

    // level.grid.borders

  }

  def scale(z: Double): Int = {
    (z * scaleFactor).toInt
  }

}

object Starter extends App {

  val levelPanel = LevelPanel(LevelLoader.load)
  ContentFrame(levelPanel)

}