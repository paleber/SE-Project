package gui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JButton, JPanel}

import akka.actor.{Actor, ActorLogging}
import model.element.LevelId
import persistence.ResourceManager.{LoadLevel, MenuLoaded}

class GuiMenu extends JPanel with Actor with ActorLogging {
  log.debug("Initializing")

  override def receive: Receive = {

    case MenuLoaded(info) =>
      log.debug("Showing menu")
      context.parent ! Gui.SetContentPane(this, "menu")

      info.foreach { case (category, names) =>
        names.foreach { name =>

          val bn = new JButton(category + " - " + name)
          bn.addActionListener(new ActionListener {
            override def actionPerformed(e: ActionEvent): Unit = {
              log.info(s"Button clicked: Level $category - $name")
              context.parent ! LoadLevel(LevelId(category, name))
            }
          })
          add(bn)
        }
      }
      revalidate()


  }

}
