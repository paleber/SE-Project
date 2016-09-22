package gui


import java.awt.Dimension
import javax.swing.{JFrame, JPanel}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import gui.Gui.SetContentPane
import msg.ServerMessage

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


object Gui {
  private val DEFAULT_SIZE = new Dimension(800, 600)

  private[gui] case class SetContentPane(c: JPanel)

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

  var content: Option[ActorRef] = None

  override def receive = {

    case SetContentPane(panel) =>
      panel.setSize(frame.getContentPane.getSize)
      frame.setContentPane(panel)

    case ServerMessage.ShowMenu =>
      content = Some(context.actorOf(Props(new GuiMenu(frame)), "menu"))

    case msg =>
      if (content.isDefined) {
        content.get ! msg
      } else {
        log.warning("Unhandled message, while no content is defined: " + msg)
      }
  }

}
