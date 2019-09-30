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
  def node_[A](
      namespace: Namespace,
      tag: String,
      ar: Seq[Parameter[A]],
      e: Seq[Html[A]]
  ): Tag[A] = {

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

  @inline
  def node[A](tag: String, namespace: Namespace = Namespace.HTML)(
      ar: Parameter[A]*
  )(e: Html[A]*): Tag[A] =
    node_(namespace, tag, ar, e)

  @inline final def div[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("div")(ar: _*)(e: _*)
  @inline final def span[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("span")(ar: _*)(e: _*)
  @inline final def a[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("a")(ar: _*)(e: _*)

  @inline final def p[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("p")(ar: _*)(e: _*)
  @inline final def text(s: String): Text = Text(s)

  @inline final def ul[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("ul")(ar: _*)(e: _*)
  @inline final def li[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("li")(ar: _*)(e: _*)

  @inline final def input[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("input")(ar: _*)(e: _*)
  @inline final def button[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("button")(ar: _*)(e: _*)

  @inline final def svg[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("svg", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def rect[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("rect", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def polyline[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("polyline", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def polygon[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("polygon", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def symbol[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("symbol", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def g[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("g", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def defs[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("defs", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def use[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("use", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def svgText[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("text", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def line[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("line", Namespace.SVG)(ar: _*)(e: _*)
  @inline final def styleTag[A](ar: Parameter[A]*)(e: Html[A]*): Tag[A] =
    node[A]("style", Namespace.HTML)(ar: _*)(e: _*)

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

  @inline final def `type`: MakeAttr = attr("type")
  @inline final def media: MakeAttr = attr("media")
  @inline final def id: MakeAttr = attr("id")
  @inline final def `class`: MakeAttr = attr("class")
  @inline final def value: MakeAttr = attr("value")
  @inline final def x: MakeAttr = attr("x")
  @inline final def y: MakeAttr = attr("y")
  @inline final def width: MakeAttr = attr("width")
  @inline final def height: MakeAttr = attr("height")
  @inline final def style: MakeAttr = attr("style")
  @inline final def transform: MakeAttr = attr("transform")
  @inline final def viewBox: MakeAttr = attr("viewBox")
  @inline final def points: MakeAttr = attr("points")
  @inline final def fill: MakeAttr = attr("fill")
  @inline final def stroke: MakeAttr = attr("stroke")
  @inline final def strokeWidth: MakeAttr = attr("stroke-width")
  @inline final def x1: MakeAttr = attr("x1")
  @inline final def x2: MakeAttr = attr("x2")
  @inline final def y1: MakeAttr = attr("y1")
  @inline final def y2: MakeAttr = attr("y2")
  @inline final def xlinkHref: MakeAttr =
    attr("xlink:href", "http://www.w3.org/1999/xlink")
  @inline final def href: MakeAttr = attr("href")
  @inline final def xmlns: MakeAttr = attr("xmlns")

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

  @inline final def onsubmit[A](msg: => A): Parameter[A] = on0("submit")(msg)
  @inline final def onclick[A](msg: => A): Parameter[A] = on0("click")(msg)

  @inline
  final def onInputElement[A](ext: HTMLInputElement => A): Parameter[A] =
    on[Event, A]("input") { (e: Event) =>
      e.target match {
        case input: HTMLInputElement =>
          ext(input)
      }
    }

  @inline
  final def oninput[A](reaction: String => A): Parameter[A] =
    onInputElement[A](i => reaction(i.value))

  @inline
  final def oncheck[A](reaction: Boolean => A): Parameter[A] =
    onInputElement[A](i => reaction(i.checked))
}
