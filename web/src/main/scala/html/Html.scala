package slimetrail.web.html

import org.scalajs.dom.raw._
import org.scalajs.dom._
import scalajs.js

/** Represente un attrinut HTML*/
object Attribut {

  /** Le namespace de l'attribut HTML. Il est sage de toujours mettre un namespace. */
  final case class Namespace(valeur: String) extends AnyVal

  /* Un attrinut HTML est en fait une paire d'un nom et d'un namespace */
  final case class Clef(valeur: String, espace: Option[Namespace])

  /** La valeur de l'attribut HTML */
  final case class Valeur(valeur: String) extends AnyVal
}

/** A fournir a addEventListener */
final case class Reaction[+A](`type`: String,
                              reaction: js.Function1[_ <: Event, A]) {
  def map[B](f: A => B): Reaction[B] =
    Reaction(`type`, reaction.andThen(f))
}

/** Namespace de noeud, soit HTML soit SVG */
sealed abstract class Namespace(val uri: String)
object Namespace {
  case object HTML extends Namespace("http://www.w3.org/1999/xhtml")
  case object SVG extends Namespace("http://www.w3.org/2000/svg")
}

/** Représente un arbre HTML/SVG dont les réactions produise des valeurs de type A*/
sealed abstract class Html[+A] {
  def map[B](f: A => B): Html[B]

  import Html._

  /** Crée un noeud du DOM correspondant à cet HTML/SVG */
  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  final def dessiner: Node =
    this match {
      case Texte(s) =>
        document.createTextNode(s)

      case Noeud(espace, balise, attributs, reactions, enfants) =>
        val b: Element = document.createElementNS(espace.uri, balise)

        attributs.foreach {
          case (Attribut.Clef(clef, ns), Attribut.Valeur(valeur)) =>
            b.setAttributeNS(ns.map(_.valeur).getOrElse(null), clef, valeur)
        }

        reactions.foreach {
          case Reaction(t, r) =>
            b.addEventListener(t, r, false)
        }

        enfants.foreach { enfant =>
          b.appendChild(enfant.dessiner)
        }

        b
    }
}

object Html {

  /** Représente un noeud Texte */
  final case class Texte(valeur: String) extends Html[Nothing] {
    def map[B](f: Nothing => B): Html[B] = this
    override def toString = valeur
  }

  /** Représente un noeud non texte */
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
  }
}
