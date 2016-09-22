package gui

import java.awt.{Color, Dimension, Graphics}
import javax.swing.{JFrame, JPanel}

import akka.actor.Actor

class GuiMenu(frame: JFrame) extends JPanel with Actor {
  context.parent ! Gui.SetContentPane(this)



  override def receive = {
    case msg =>
  }

  override def paint(g: Graphics): Unit = {
    g.setColor(Color.CYAN)
    g.fillRect(20, 20, 200, 90)

    g.setColor(Color.RED)
    g.drawRect(0, 0, 799, 599)

    g.drawString(System.currentTimeMillis().toString, 10, 20)
  }

}
