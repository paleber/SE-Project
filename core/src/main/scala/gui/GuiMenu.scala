package gui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JButton, JLabel, JPanel}

import akka.actor.{Actor, ActorLogging}
import msg.ClientMessage

class GuiMenu extends JPanel with Actor with ActorLogging{
  context.parent ! Gui.SetContentPane(this)

  private case class StartLevelEvent(level: Int) extends ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      log.info(s"Button clicked: Level $level")
      context.parent ! ClientMessage.ShowGame(level)
    }
  }

  for(i <- 0 to 20) {
    val bnLevel = new JButton(s"Level $i")
    bnLevel.addActionListener(StartLevelEvent(i))
    add(bnLevel)
  }

  override def receive = {
    case msg => log.warning("Unhandled message: " + msg)
  }

}
