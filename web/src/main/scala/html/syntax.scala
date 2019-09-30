package slimetrail.web.html

import org.scalajs.dom.raw.{Event, HTMLInputElement}
import Html._
import scala.scalajs.js

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
object syntax {

  sealed abstract class Parametre[+A]
  object Parametre {
    final case class Attr(attr: (Attribut.Clef, Attribut.Valeur))
        extends Parametre[Nothing]
    final case class Reac[+A](reac: Reaction[A]) extends Parametre[A]
    final case object Nop extends Parametre[Nothing]
  }

  def noeud_[A](
      espaceDeNom: Namespace,
      balise: String,
      ar: Seq[Parametre[A]],
      e: Seq[Html[A]]
  ): Noeud[A] = {
    val attributs: Map[Attribut.Clef, Attribut.Valeur] =
      ar.flatMap {
        case Parametre.Attr(cd) => List(cd)
        case _                  => Nil
      }.toMap

    val reactions: Seq[Reaction[A]] =
      ar.flatMap {
        case Parametre.Reac(r) => List(r)
        case _                 => Nil
      }

    Noeud(espaceDeNom, balise, attributs, reactions, e)
  }
  @inline
  def noeud[A](
      balise: String,
      espaceDeNom: Namespace = Namespace.HTML
  )(ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud_(espaceDeNom, balise, ar, e)

  @inline def div[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("div")(ar: _*)(e: _*)
  @inline def span[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("span")(ar: _*)(e: _*)
  @inline def a[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("a")(ar: _*)(e: _*)

  def p[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("p")(ar: _*)(e: _*)
  def texte(s: String): Texte =
    Texte(s)

  def ul[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("ul")(ar: _*)(e: _*)
  def li[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("li")(ar: _*)(e: _*)
  def input[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("input")(ar: _*)(e: _*)
  def button[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("button")(ar: _*)(e: _*)

  def svg[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("svg", Namespace.SVG)(ar: _*)(e: _*)
  def rect[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("rect", Namespace.SVG)(ar: _*)(e: _*)
  def polyline[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("polyline", Namespace.SVG)(ar: _*)(e: _*)
  def polygon[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("polygon", Namespace.SVG)(ar: _*)(e: _*)
  def symbol[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("symbol", Namespace.SVG)(ar: _*)(e: _*)
  def g[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("g", Namespace.SVG)(ar: _*)(e: _*)
  def defs[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("defs", Namespace.SVG)(ar: _*)(e: _*)
  def use[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("use", Namespace.SVG)(ar: _*)(e: _*)
  def text[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("text", Namespace.SVG)(ar: _*)(e: _*)
  def line[A](ar: Parametre[A]*)(e: Html[A]*): Noeud[A] =
    noeud[A]("line", Namespace.SVG)(ar: _*)(e: _*)

  val nop: Parametre[Nothing] = Parametre.Nop

  type FaitAttr = String => Parametre[Nothing]

  @inline
  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  def attr(clef: String, espace: String = ""): FaitAttr =
    v => {
      val ns: Option[Attribut.Namespace] =
        if (espace.isEmpty)
          None
        else
          Some(Attribut.Namespace(espace))

      Parametre.Attr(
        (Attribut.Clef(clef, ns), Attribut.Valeur(v))
      )
    }

  def `type`: FaitAttr = attr("type")
  def id: FaitAttr = attr("id")
  def `class`: FaitAttr = attr("class")
  def value: FaitAttr = attr("value")
  def x: FaitAttr = attr("x")
  def y: FaitAttr = attr("y")
  def width: FaitAttr = attr("width")
  def height: FaitAttr = attr("height")
  def style: FaitAttr = attr("style")
  def transform: FaitAttr = attr("transform")
  def viewBox: FaitAttr = attr("viewBox")
  def points: FaitAttr = attr("points")
  def fill: FaitAttr = attr("fill")
  def stroke: FaitAttr = attr("stroke")
  def strokeWidth: FaitAttr = attr("stroke-width")
  def x1: FaitAttr = attr("x1")
  def x2: FaitAttr = attr("x2")
  def y1: FaitAttr = attr("y1")
  def y2: FaitAttr = attr("y2")
  def xlinkHref: FaitAttr = attr("xlink:href", "http://www.w3.org/1999/xlink")
  def href: FaitAttr = attr("href")

  def checked(b: Boolean): Parametre[Nothing] =
    if (b)
      attr("checked")("checked")
    else
      Parametre.Nop

  type FaitReaction[A] = js.Function1[_ <: Event, A] => Parametre[A]

  @inline
  def on[T <: Event, A](`type`: String)(f: js.Function1[T, A]): Parametre[A] =
    Parametre.Reac(Reaction(`type`, (e: T) => {
      e.stopPropagation()
      f(e)
    }))

  @inline
  def on0[A](`type`: String)(msg: => A): Parametre[A] =
    on[Event, A](`type`) { _ =>
      msg
    }

  @inline def onsubmit[A](msg: => A): Parametre[A] = on0("submit")(msg)
  @inline def onclick[A](msg: => A): Parametre[A] = on0("click")(msg)

  @inline
  def onInputElement[A](ext: HTMLInputElement => A): Parametre[A] =
    on[Event, A]("input") { (e: Event) =>
      e.target match {
        case input: HTMLInputElement =>
          ext(input)
      }
    }

  @inline
  def oninput[A](reaction: String => A): Parametre[A] =
    onInputElement[A](i => reaction(i.value))

  @inline
  def oncheck[A](reaction: Boolean => A): Parametre[A] =
    onInputElement[A](i => reaction(i.checked))
}
