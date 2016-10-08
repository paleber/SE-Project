package gui


import java.awt.Dimension
import javax.swing.{JFrame, JPanel}

import akka.actor.{Actor, ActorLogging, Props}
import gui.Gui.SetContentPane
import model.element.Level
import model.general.{DefaultActor, IdGenerator}
import model.msg.{ClientMessage, ServerMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


object Gui {

  private val DEFAULT_SIZE = new Dimension(800, 600)

  private[gui] case class SetContentPane(c: JPanel, name: String)

}

class Gui extends Actor with ActorLogging {
  log.debug("Initializing")

  val main = context.actorSelection("../control")

  val frame = new JFrame()
  frame.setLayout(null)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.getContentPane.setPreferredSize(Gui.DEFAULT_SIZE)
  frame.pack()
  frame.setLocationRelativeTo(null)
  frame.setVisible(true)

  context.system.scheduler.schedule(0 millis, 16 millis) {
    frame.repaint()
  }

  var content = context.actorOf(Props[DefaultActor], "init")

  override def receive = {

    case SetContentPane(panel, name) =>
      frame.setTitle(s"scongo - $name")
      panel.setSize(frame.getContentPane.getSize)
      frame.setContentPane(panel)
      context.system.scheduler.scheduleOnce(100 millis) {
        panel.revalidate()
      }
      panel.setFocusable(true)
      panel.requestFocusInWindow()

    case ServerMessage.ShowMenu =>
      context.stop(content)
      content = context.actorOf(Props[GuiMenu], s"menu-${IdGenerator.generate()}")

    case ServerMessage.ShowGame(levelName: String, level: Level) =>
      content = context.actorOf(Props(GuiGame(levelName,level)), s"game-${IdGenerator.generate()}")

    case msg: ServerMessage => content ! msg

    case msg: ClientMessage => main ! msg

    case msg => log.warning("Unhandled message: " + msg)

  }

}
