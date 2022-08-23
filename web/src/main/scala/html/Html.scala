package slimetrail.web.html

import org.scalajs.dom._
import scalajs.js

/** An HTML Attribute */
object Attribute {

  /** The namespace of the HTML attribute. It is wise to always set the namespace. */
  final case class Namespace(value: String) extends AnyVal

  /** An HTML attribute is actually a pair of a name and a namespace */
  final case class Key(value: String, namespace: Option[Namespace])

  /** The value of the HTML attribute */
  final case class Value(value: String) extends AnyVal
}

/** To be given to addEventListener */
final case class Reaction[+A](
    `type`: String,
    reaction: js.Function1[? <: Event, A]
) {
  def map[B](f: A => B): Reaction[B] =
    Reaction(`type`, reaction.andThen(f))
}

/** Namespace of the tag node, either HTML or SVG */
sealed abstract class Namespace(val uri: String)
object Namespace {
  case object HTML extends Namespace("http://www.w3.org/1999/xhtml")
  case object SVG  extends Namespace("http://www.w3.org/2000/svg")
}

/** Represents an HTML/SVG tree whose reactions produce values of type A */
sealed abstract class Html[+A] {
  def map[B](f: A => B): Html[B]

  import Html._

  /** Draw a DOM node corresponding to this HTML/SVG tree */
  @SuppressWarnings(Array("org.wartremover.warts.Null", "org.wartremover.warts.GetOrElseNull"))
  final def draw: Node =
    this match {
      case Text(s) =>
        document.createTextNode(s)

      case Tag(namespace, tag, attributes, reactions, children) =>
        val b: Element = document.createElementNS(namespace.uri, tag)

        attributes.foreach { case (Attribute.Key(clef, ns), Attribute.Value(valeur)) =>
          b.setAttributeNS(ns.map(_.value).getOrElse(null), clef, valeur)
        }

        reactions.foreach { case Reaction(t, r) =>
          b.addEventListener(t, r, false)
        }

        children.foreach { children =>
          b.appendChild(children.draw)
        }

        b
    }
}

object Html {

  /** Represents a texte node */
  final case class Text(value: String) extends Html[Nothing] {
    def map[B](f: Nothing => B): Html[B] = this
    override def toString: String        = value
  }

  /** Represents a tag node */
  final case class Tag[+A](
      namespace: Namespace,
      tag: String,
      attributes: Map[Attribute.Key, Attribute.Value],
      reactions: Seq[Reaction[A]],
      children: Seq[Html[A]]
  ) extends Html[A] {

    def map[B](f: A => B): Html[B] =
      Tag(
        namespace,
        tag,
        attributes,
        reactions.map(_.map(f)),
        children.map(_.map(f))
      )
  }
}
