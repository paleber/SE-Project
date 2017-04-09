package gui


import java.awt.Dimension
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{JFrame, JPanel}

import akka.actor.{Actor, ActorLogging, Props}
import control.UserControl.Shutdown
import gui.Gui.SetContentPane
import model.msg.{ClientMsg, ServerMsg}
import persistence.ResourceManager.{LevelLoaded, MenuLoaded}
import scaldi.Injector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


object Gui {

  private val DEFAULT_SIZE = new Dimension(800, 600)

  private[gui] case class SetContentPane(c: JPanel, name: String)

}

final class Gui(implicit inj: Injector) extends Actor with ActorLogging {
  log.debug("Initializing")

  private val frame = new JFrame()
  frame.setLayout(null)
  frame.getContentPane.setPreferredSize(Gui.DEFAULT_SIZE)
  frame.pack()
  frame.setLocationRelativeTo(null)
  frame.setVisible(true)
  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      context.parent ! Shutdown
    }
  })

  context.system.scheduler.schedule(0 millis, 16 millis) {
    frame.repaint()
  }

  private val menu = context.actorOf(Props[GuiMenu], "menu")
  private val game = context.actorOf(GuiGame.props, "game")

  private var content = context.actorOf(Props.empty, "init")

  override def receive: Receive = {

    case SetContentPane(panel, name) =>
      frame.setTitle(s"scongo - $name")
      panel.setSize(frame.getContentPane.getSize)
      frame.setContentPane(panel)
      panel.setFocusable(true)
      panel.requestFocusInWindow()

    case msg: MenuLoaded =>
      content = menu
      content ! msg

    case msg: LevelLoaded =>
      content = game
      content ! msg

    case msg: ServerMsg =>
      content ! msg

    case msg: ClientMsg =>
      context.parent ! msg

  }

  override def postStop: Unit = {
    frame.dispose()
  }

}
