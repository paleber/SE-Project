package controllers

import akka.actor.Actor
import msg.ServerMessage
import play.api.mvc._

class WuiConsole extends Actor {
  override def receive = {
    case msg : ServerMessage =>
    case

  }
}

/**
 * Minimal controller examples that output text/plain responses.
 */
class Application extends Controller {

  def index = Action {
    Ok("Hello world")
  }

  def hello(name: String) = Action {
    Ok("Hello " + name)
  }

  def tui = Action {
    Ok("TODO")
  }

}

/**
 * Alternate controller that renders a template. This example is a separate class so the action method name
 * can also be ‘hello’.
 */
class Application2 extends Controller {

  def hello(name: String) = Action {
    Ok(views.html.hello(name))
  }
}
