package slimetrail.web

import org.scalajs.dom
import org.scalajs.dom._
import dom.document

object Main {
  def auChargement(a: => Unit): Unit =
    dom.document.addEventListener("DOMContentLoaded", (_: Event) => a)

  def main(args: Array[String]): Unit =
    auChargement {
      SlimetrailApp.executer(document.getElementById("espaceScalaJS"), true)
    }
}
