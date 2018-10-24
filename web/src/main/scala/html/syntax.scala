package slimetrail.web.html

import org.scalajs.dom.raw.{Event, HTMLInputElement}
import Html._
import scala.scalajs.js

/** Small DSL to write HTML/SVG trees as if it would actually be HTML/SVG */
@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
object syntax {

  /** A parameter is either:
    *  - an attribute that will be attached to the corresponding node.
    *  - a reaction that will be added to the event listener of the given node.
    *  - Nop that has no effect on the produced HTML/SVG, but it is convenient syntactic wise.
    */
  sealed abstract class Parameter[+A]
  object Parameter {
    final case class Attr(attr: (Attribute.Key, Attribute.Value))
        extends Parameter[Nothing]
    final case class Reac[+A](reac: Reaction[A]) extends Parameter[A]
    final case object Nop extends Parameter[Nothing]
  }

  /** Node creation helper */
  def node_[A](namespace: Namespace, tag: String)(ar: Seq[Parameter[A]])(
      e: Seq[Html[A]]): Tag[A] = {

    val attributes: Map[Attribute.Key, Attribute.Value] =
      ar.flatMap {
        case Parameter.Attr(cd) => List(cd)
        case _                  => Nil
      }.toMap

    val reactions: Seq[Reaction[A]] =
      ar.flatMap {
        case Parameter.Reac(r) => List(r)
        case _                 => Nil
      }

    Tag(namespace, tag, attributes, reactions, e)
  }

  /** Type of node builders */
  type MakeNode[A] = (Parameter[A]*) => (Html[A]*) => Tag[A]

  @inline
  def node[A](tag: String, namespace: Namespace = Namespace.HTML): MakeNode[A] =
    node_(namespace, tag) _

  def div[A]: MakeNode[A] = node[A]("div")
  def span[A]: MakeNode[A] = node[A]("span")
  def a[A]: MakeNode[A] = node[A]("a")

  def p[A]: MakeNode[A] = node[A]("p")
  def text(s: String): Text = Text(s)

  def ul[A]: MakeNode[A] = node[A]("ul")
  def li[A]: MakeNode[A] = node[A]("li")

  def input[A]: MakeNode[A] = node[A]("input")
  def button[A]: MakeNode[A] = node[A]("button")

  def svg[A]: MakeNode[A] = node[A]("svg", Namespace.SVG)
  def rect[A]: MakeNode[A] = node[A]("rect", Namespace.SVG)
  def polyline[A]: MakeNode[A] = node[A]("polyline", Namespace.SVG)
  def polygon[A]: MakeNode[A] = node[A]("polygon", Namespace.SVG)
  def symbol[A]: MakeNode[A] = node[A]("symbol", Namespace.SVG)
  def g[A]: MakeNode[A] = node[A]("g", Namespace.SVG)
  def defs[A]: MakeNode[A] = node[A]("defs", Namespace.SVG)
  def use[A]: MakeNode[A] = node[A]("use", Namespace.SVG)
  def svgText[A]: MakeNode[A] = node[A]("text", Namespace.SVG)
  def line[A]: MakeNode[A] = node[A]("line", Namespace.SVG)

  val nop: Parameter[Nothing] = Parameter.Nop

  /** Type of attribute builders */
  type MakeAttr = String => Parameter[Nothing]

  @inline
  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  def attr(clef: String, namespace: String = ""): MakeAttr =
    v => {
      val ns: Option[Attribute.Namespace] =
        if (namespace.isEmpty)
          None
        else
          Some(Attribute.Namespace(namespace))

      Parameter.Attr(
        (Attribute.Key(clef, ns), Attribute.Value(v))
      )
    }

  def `type`: MakeAttr = attr("type")
  def id: MakeAttr = attr("id")
  def `class`: MakeAttr = attr("class")
  def value: MakeAttr = attr("value")
  def x: MakeAttr = attr("x")
  def y: MakeAttr = attr("y")
  def width: MakeAttr = attr("width")
  def height: MakeAttr = attr("height")
  def style: MakeAttr = attr("style")
  def transform: MakeAttr = attr("transform")
  def viewBox: MakeAttr = attr("viewBox")
  def points: MakeAttr = attr("points")
  def fill: MakeAttr = attr("fill")
  def stroke: MakeAttr = attr("stroke")
  def strokeWidth: MakeAttr = attr("stroke-width")
  def x1: MakeAttr = attr("x1")
  def x2: MakeAttr = attr("x2")
  def y1: MakeAttr = attr("y1")
  def y2: MakeAttr = attr("y2")
  def xlinkHref: MakeAttr = attr("xlink:href", "http://www.w3.org/1999/xlink")
  def href: MakeAttr = attr("href")

  def checked(b: Boolean): Parameter[Nothing] =
    if (b)
      attr("checked")("checked")
    else
      Parameter.Nop

  /** Type of reaction builders */
  type MakeReaction[A] = js.Function1[_ <: Event, A] => Parameter[A]

  @inline
  def on[T <: Event, A](`type`: String)(f: js.Function1[T, A]): Parameter[A] =
    Parameter.Reac(Reaction(`type`, (e: T) => {
      e.stopPropagation()
      f(e)
    }))

  @inline
  def on0[A](`type`: String)(msg: => A): Parameter[A] =
    on[Event, A](`type`) { _ =>
      msg
    }

  @inline def onsubmit[A](msg: => A): Parameter[A] = on0("submit")(msg)
  @inline def onclick[A](msg: => A): Parameter[A] = on0("click")(msg)

  @inline
  def onInputElement[A](ext: HTMLInputElement => A): Parameter[A] =
    on[Event, A]("input") { (e: Event) =>
      e.target match {
        case input: HTMLInputElement =>
          ext(input)
      }
    }

  @inline
  def oninput[A](reaction: String => A): Parameter[A] =
    onInputElement[A](i => reaction(i.value))

  @inline
  def oncheck[A](reaction: Boolean => A): Parameter[A] =
    onInputElement[A](i => reaction(i.checked))
}
