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

  type FaitNoeud[A] = (Parametre[A]*) => (Html[A]*) => Noeud[A]

  def noeud_[A](espaceDeNom: Namespace, balise: String)(ar: Seq[Parametre[A]])(
      e: Seq[Html[A]]): Noeud[A] = {
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
  def noeud[A](balise: String,
               espaceDeNom: Namespace = Namespace.HTML): FaitNoeud[A] =
    noeud_(espaceDeNom, balise) _

  def div[A]: FaitNoeud[A] = noeud[A]("div")
  def span[A]: FaitNoeud[A] = noeud[A]("span")

  def p[A]: FaitNoeud[A] = noeud[A]("p")
  def texte(s: String): Texte = Texte(s)

  def ul[A]: FaitNoeud[A] = noeud[A]("ul")
  def li[A]: FaitNoeud[A] = noeud[A]("li")

  def input[A]: FaitNoeud[A] = noeud[A]("input")
  def button[A]: FaitNoeud[A] = noeud[A]("button")

  def svg[A]: FaitNoeud[A] = noeud[A]("svg", Namespace.SVG)
  def rect[A]: FaitNoeud[A] = noeud[A]("rect", Namespace.SVG)
  def polyline[A]: FaitNoeud[A] = noeud[A]("polyline", Namespace.SVG)
  def polygon[A]: FaitNoeud[A] = noeud[A]("polygon", Namespace.SVG)
  def symbol[A]: FaitNoeud[A] = noeud[A]("symbol", Namespace.SVG)
  def g[A]: FaitNoeud[A] = noeud[A]("g", Namespace.SVG)
  def defs[A]: FaitNoeud[A] = noeud[A]("defs", Namespace.SVG)
  def use[A]: FaitNoeud[A] = noeud[A]("use", Namespace.SVG)
  def text[A]: FaitNoeud[A] = noeud[A]("text", Namespace.SVG)
  def line[A]: FaitNoeud[A] = noeud[A]("line", Namespace.SVG)

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

  def checked(b: Boolean): Parametre[Nothing] =
    if (b)
      attr("checked")("checked")
    else
      Parametre.Nop

  type FaitReaction[A] = js.Function1[_ <: Event, A] => Parametre[A]

  def on[A](`type`: String): FaitReaction[A] =
    (f: js.Function1[_ <: Event, A]) => Parametre.Reac(Reaction(`type`, f))

  def on0[A](`type`: String)(msg: => A): Parametre[A] =
    Parametre.Reac(Reaction(`type`, (_: Event) => msg))

  def onsubmit[A](msg: => A): Parametre[A] = on0("submit")(msg)
  def onclick[A](msg: => A): Parametre[A] = on0("click")(msg)

  def onInputElement[A](ext: HTMLInputElement => A): Parametre[A] =
    Parametre.Reac(
      Reaction("input",
               (e: Event) =>
                 e.target match {
                   case input: HTMLInputElement =>
                     ext(input)
               })
    )

  @inline
  def oninput[A](reaction: String => A): Parametre[A] =
    onInputElement[A](i => reaction(i.value))

  @inline
  def oncheck[A](reaction: Boolean => A): Parametre[A] =
    onInputElement[A](i => reaction(i.checked))
}
