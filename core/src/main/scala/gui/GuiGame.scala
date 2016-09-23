package gui

import java.awt.{Color, Graphics}
import javax.swing.JPanel

import akka.actor.{Actor, ActorLogging}
import model.Level

case class GuiGame(level: Level) extends JPanel with Actor with ActorLogging{
  log.debug("Initializing")
  context.parent ! Gui.SetContentPane(this)


  override def paint(g: Graphics) = {
    super.paint(g)

    g.setColor(Color.GREEN)
    g.fillRect(100, 100, 300, 100)
  }

  override def receive = {
    case msg => log.warning("Unhandled message: " + msg)
  }

}
