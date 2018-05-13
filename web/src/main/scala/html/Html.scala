package slimetrail.web.html

import org.scalajs.dom.raw._
import scalajs.js

object Attribut {
  final case class Clef(valeur: String, espace: Option[Namespace])
  final case class Valeur(valeur: String) extends AnyVal
  final case class Namespace(valeur: String) extends AnyVal
}

final case class Reaction[+A](`type`: String,
                              reaction: js.Function1[_ <: Event, A]) {
  def map[B](f: A => B): Reaction[B] =
    Reaction(`type`, reaction.andThen(f))

  override def toString = s"""on${`type`}="""""
}

sealed abstract class Namespace(val uri: String)
object Namespace {
  case object HTML extends Namespace("http://www.w3.org/1999/xhtml")
  case object SVG extends Namespace("http://www.w3.org/2000/svg")
}

sealed abstract class Html[+A] {
  def map[B](f: A => B): Html[B]
}

object Html {
  final case class Texte(valeur: String) extends Html[Nothing] {
    def map[B](f: Nothing => B): Html[B] = this
    override def toString = valeur
  }

  final case class Noeud[+A](
      espaceDeNom: Namespace,
      balise: String,
      attributs: Map[Attribut.Clef, Attribut.Valeur],
      reactions: Seq[Reaction[A]],
      enfants: Seq[Html[A]]
  ) extends Html[A] {

    def map[B](f: A => B): Html[B] =
      Noeud(
        espaceDeNom,
        balise,
        attributs,
        reactions.map(_.map(f)),
        enfants.map(_.map(f))
      )

    override def toString = {
      val attrs =
        attributs.map {
          case (Attribut.Clef(c, ns), Attribut.Valeur(v)) =>
            val sns = ns.map(x => s"[$x]").getOrElse("")
            s""" $sns$c="$v""""
        }.mkString
      s"<$balise xmlns:$espaceDeNom$attrs/>"
    }
  }
}
