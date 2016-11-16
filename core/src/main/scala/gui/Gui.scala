package gui


import java.awt.Dimension
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.{JFrame, JPanel, WindowConstants}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import control.MainControl
import gui.Gui.SetContentPane
import model.element.Game
import model.general.{DefaultActor, IdGenerator}
import model.msg.{ClientMsg, ServerMsg}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


object Gui {

  private val DEFAULT_SIZE = new Dimension(800, 600)

  private[gui] case class SetContentPane(c: JPanel, name: String)

  def props(control: ActorRef) = Props(new Gui(control))

}

private class Gui(control: ActorRef) extends Actor with ActorLogging {
  log.debug("Initializing")

  control ! MainControl.RegisterView(self)

  private val frame = new JFrame()
  frame.setLayout(null)
  frame.getContentPane.setPreferredSize(Gui.DEFAULT_SIZE)
  frame.pack()
  frame.setLocationRelativeTo(null)
  frame.setVisible(true)
  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) = {
      control ! PoisonPill
    }
  })

  context.system.scheduler.schedule(0 millis, 16 millis) {
    frame.repaint()
  }

  private var content = context.actorOf(Props[DefaultActor], "init")

  override def receive = {

    case SetContentPane(panel, name) =>
      frame.setTitle(s"scongo - $name")
      panel.setSize(frame.getContentPane.getSize)
      frame.setContentPane(panel)
      panel.setFocusable(true)
      panel.requestFocusInWindow()

    case ServerMsg.ShowMenu =>
      context.stop(content)
      content = context.actorOf(Props[GuiMenu], s"menu-${IdGenerator.generate()}")

    case ServerMsg.ShowGame(game: Game) =>
      content = context.actorOf(Props(GuiGame(game)), s"game-${IdGenerator.generate()}")

    case msg: ServerMsg => content ! msg

    case msg: ClientMsg => control ! msg

    case msg => log.warning("Unhandled message: " + msg)

  }

  override def postStop = {
    frame.dispose()
  }

}
