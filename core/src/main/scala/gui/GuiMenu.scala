package gui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JButton, JPanel}

import akka.actor.{Actor, ActorLogging}
import model.plan.LevelPlan
import msg.ClientMessage

class GuiMenu extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")
  context.parent ! Gui.SetContentPane(this)

  private case class StartLevelEvent(level: String) extends ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      log.info(s"Button clicked: Level $level")
      context.parent ! ClientMessage.ShowGame(level)
    }
  }

  for(level <- LevelPlan.map.keys) {
    val bnLevel = new JButton(level)
    bnLevel.addActionListener(StartLevelEvent(level))
    add(bnLevel)
  }



  override def receive = {
    case msg => log.warning("Unhandled message: " + msg)
  }

}
