package slimetrail.web

package object html {
  implicit final class NodeOps(self: org.scalajs.dom.raw.Node) {
    def outerHTML: String =
      self match {
        case e: org.scalajs.dom.raw.HTMLElement =>
          val ns =
            for {
              uri <- Option(e.namespaceURI)
              n <- ".*/([^/]+)$".r.findFirstMatchIn(uri)
            } yield n.group(1).trim.toUpperCase

          val s = s"${e.outerHTML.trim.take(30)}..."

          ns match {
            case Some(ns) => s"$ns($s)"
            case _        => s
          }
        case e: org.scalajs.dom.raw.Text =>
          s"""${e.wholeText.trim.take(10)}..."""
        case _ => "![Not an HTML element]"
      }
  }
}
