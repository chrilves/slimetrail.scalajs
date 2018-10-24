package slimetrail.web

import org.scalajs.dom
import org.scalajs.dom._
import dom.document

object Main {
  def onLoading(a: => Unit): Unit =
    dom.document.addEventListener("DOMContentLoaded", (_: Event) => a)

  def main(args: Array[String]): Unit =
    onLoading {
      new SlimetrailWebApp(10)
        .run(document.getElementById("espaceScalaJS"))
    }
}
