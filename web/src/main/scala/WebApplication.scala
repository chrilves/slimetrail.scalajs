package slimetrail.web

import org.scalajs.dom.raw._
import slimetrail.web.html._
import slimetrail._

/** A Web Application.
  * Just like a Text Application but the view return an Html tree
  */
trait WebApplication extends Application { self =>

  /** The view returns an Html tree */
  def view(model: Model): slimetrail.web.html.Html[Msg]

  /** Run a Web Application on a given node
    *
    * The principle is simple:
    *  - Just like any {{{Application}}}, at any given time there is a
    *    "current model" which is the state of the application, a value of type [[Model]].
    *  - The function [[view]] compute the Html tree corresponding the current value of the model.
    *  - This tree enables building a Node of the DOM
    *  - This new Node replaces the old one.
    *  - Each event (thus any reaction executed) returns a un message (of type [[Msg]]).
    *  - This message updates the state of the application via the [[update]] function.
    *  - The new state becomes the new current state and we loop.
    */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final def run(initialNode: Node): Unit = {
    import Html._

    final case class State(node: Node, view: Html[Unit], model: Model)

    var state: State =
      State(initialNode, Text(""), initialModel)

    def actualize(newModel: Model): Unit = {
      log(s"[Application] new model: $newModel")
      val newView: Html[Unit] =
        view(newModel).map { msg: Msg =>
          log(s"[Application] new message: $msg")
          actualize(update(msg, state.model))
        }

      val oldNode: Node = state.node
      val parent: Node = oldNode.parentNode

      val newNode: Node = {
        val n = newView.draw
        parent.replaceChild(n, oldNode)
        n
      }

      state = State(newNode, newView, newModel)
    }

    actualize(initialModel)
  }
}
