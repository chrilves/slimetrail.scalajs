package slimetrail.text

import scala.annotation._
import slimetrail._

/** Specialization of an {{{slimetrail.Application}}} to a text interface.*/
trait TextApplication extends Application { self =>

  /** The view must print the current state of the application, given by {{{model}}}
    * and produce a message [[Msg]] so that the application can move to its next state.
    */
  def view(model: Model): Msg

  /** Run an application.*/
  final def run(): Unit = {
    @tailrec
    def loop(model: Model): Unit = {
      val msg: Msg = view(model)
      val nextModel: Model = update(msg, model)
      loop(nextModel)
    }

    loop(initialModel)
  }
}
