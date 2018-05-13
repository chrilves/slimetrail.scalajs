package slimetrail.web.html

import org.scalajs.dom
import dom.document
import org.scalajs.dom.raw._
import org.scalajs.dom.ext._
import outils._
import Html._

sealed abstract class Nature
object Nature {
  final case class Texte(valeur: String) extends Nature
  final case class NoeudSansId(espace: Namespace, balise: String) extends Nature
  final case class Id(espace: Namespace, balise: String, id: Attribut.Valeur)
      extends Nature

  def apply[A](html: Html[A]): Nature =
    html match {
      case Html.Texte(s) =>
        Texte(s)
      case Html.Noeud(espace, balise, attrs, _, _) =>
        attrs.get(Attribut.Clef("id", None)) match {
          case Some(id) => Id(espace, balise, id)
          case None     => NoeudSansId(espace, balise)
        }
    }
}

object Rendu {

  val debug = false

  @inline private def log(s: => String): Unit =
    if (debug) println(s)

  final case class Entree(html: Html[Unit], noeud: Node) {
    override def toString = s"Entree(html=$html, noeud=${noeud.outerHTML})"
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def dessiner(h: Html[Unit]): Node = {
    log(s"[dessiner] dessin de $h")

    h match {
      case Texte(s) =>
        document.createTextNode(s)

      case Noeud(espace, balise, attributs, reactions, enfants) =>
        val b: Element = document.createElementNS(espace.uri, balise)

        attributs.foreach {
          case (Attribut.Clef(clef, ns), Attribut.Valeur(valeur)) =>
            log(s"""[dessiner]   $clef="$valeur" [$ns]""")
            b.setAttributeNS(ns.map(_.valeur).getOrElse(null), clef, valeur)
        }

        reactions.foreach {
          case Reaction(t, r) =>
            b.addEventListener(t, r)
        }

        enfants.foreach { enfant =>
          b.appendChild(dessiner(enfant))
        }

        b
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def actualiserNoeud(
      noeud: Element,
      anciens_attributs: Map[Attribut.Clef, Attribut.Valeur],
      anciennes_reactions: Seq[Reaction[Unit]],
      nouveaux_attributs: Map[Attribut.Clef, Attribut.Valeur],
      nouvelles_reactions: Seq[Reaction[Unit]],
  ): Unit = {
    log(s"[actualiserNoeud] noeud=${noeud.outerHTML}")

    // Ablation des anciennes reactions
    anciennes_reactions.foreach {
      case re @ Reaction(t, r) =>
        log(s"[actualiserNoeud]  - Suppression réaction $re")
        noeud.removeEventListener(t, r)
    }

    // Mise en place des nouvelles
    nouvelles_reactions.foreach {
      case re @ Reaction(t, r) =>
        log(s"[actualiserNoeud]  - Ajour réaction $re")
        noeud.addEventListener(t, r)
    }

    // Attributs enlevés
    (anciens_attributs.keys.toSet -- nouveaux_attributs.keys).foreach {
      case Attribut.Clef(clef, ns) =>
        log(s"[actualiserNoeud]  - Suppression attribut $clef [$ns]")
        noeud.removeAttributeNS(ns.map(_.valeur).getOrElse(null), clef)
    }

    // Attribus mis a jour et ajoutés
    nouveaux_attributs.foreach {
      case (c @ Attribut.Clef(clef, ns), v @ Attribut.Valeur(valeur)) =>
        anciens_attributs.get(c) match {
          case Some(av) if av === v =>
            log(s"[actualiserNoeud]  - Même attribut $clef")
            ()
          case Some(_) =>
            log(
              s"""[actualiserNoeud]  - Définition jour de l'attribut $clef="${valeur}"""")
            noeud.setAttributeNS(ns.map(_.valeur).getOrElse(null), clef, valeur)
        }
    }
  }

  def difference(
      parent: Node,
      ancien: Entree,
      nouveauHtml: Html[Unit]
  ): Node = {
    log(s"[difference] parent=${parent.outerHTML}")
    log(s"[difference] ancien=$ancien")
    log(s"[difference] nouveauHtml=$nouveauHtml")

    nouveauHtml match {
      case Texte(s) =>
        ancien match {
          case Entree(Texte(u), text: dom.raw.Text) =>
            log("[difference] ancien noeud texte")
            if (u =/= s) text.textContent = s
            text
          case _ =>
            log("[difference] nouveau noeud texte")
            val nouveauNoeud = document.createTextNode(s)
            parent.replaceChild(nouveauNoeud, ancien.noeud)
            nouveauNoeud
        }

      case Noeud(espace, balise, attributs, reactions, nouveaux_enfants) =>
        ancien match {
          case Entree(
              Noeud(a_espace, a_balise, a_attrs, a_reactions, anciens_enfants),
              ancien_element: dom.raw.Element)
              if a_balise === balise && a_espace === espace =>
            log(s"[difference] même noeud element")
            actualiserNoeud(ancien_element,
                            a_attrs,
                            a_reactions,
                            attributs,
                            reactions)

            val anciennes_entrees: Array[Entree] =
              anciens_enfants.toArray
                .zip(ancien_element.childNodes)
                .map { case (h, n) => Entree(h, n) }

            def memeNature(g: Entree, d: Html[Unit]): Boolean =
              Nature(g.html) === Nature(d)

            val diffs: List[Diff[Entree, Html[Unit]]] =
              Diff
                .myers(memeNature _)(anciennes_entrees,
                                     nouveaux_enfants.toArray)
                ._2

            anciennes_entrees.zipWithIndex.foreach {
              case (e, i) =>
                log(s"[difference] anciennes_entree[$i]=$e [${Nature(e.html)}]")
            }

            nouveaux_enfants.zipWithIndex.foreach {
              case (e, i) =>
                log(s"[difference] nouveaux_enfants[$i]=$e [${Nature(e)}]")
            }

            diffs.zipWithIndex.foreach {
              case (e, i) =>
                log(s"[difference] diffs[$i]=$e")
            }

            import Diff._

            @scala.annotation.tailrec
            def appliquer(dernier: Option[Node],
                          l: List[Diff[Entree, Html[Unit]]]): Unit =
              l match {
                case Nil =>
                  ()

                case Suppr(a) :: Ajout(b) :: tl =>
                  appliquer(dernier, Rempl(a, b) :: tl)

                case Rempl(entree, h2) :: tl =>
                  log(s"[difference] [Rempl] $entree => $h2")
                  val nouveau = dessiner(h2)
                  val nvDernier =
                    ancien.noeud.replaceChild(nouveau, entree.noeud)
                  appliquer(Some(nvDernier), tl)

                case Suppr(entree) :: tl =>
                  log(s"[difference] [Suppr] $entree")
                  ancien.noeud.removeChild(entree.noeud)
                  appliquer(dernier, tl)

                case Ajout(h2) :: tl =>
                  log(s"[difference] [Ajout] $h2")
                  val nouveau = dessiner(h2)
                  val nvDernier =
                    dernier match {
                      case Some(n) =>
                        ancien.noeud.insertBefore(nouveau, n)
                      case _ =>
                        ancien.noeud.appendChild(nouveau)
                    }

                  appliquer(Some(nvDernier), tl)

                case Ident(entree, h2) :: tl =>
                  log(s"[difference] [Ident] $entree => $h2")
                  val nvDernier = difference(ancien.noeud, entree, h2)
                  appliquer(Some(nvDernier), tl)
              }
            appliquer(None, diffs.reverse)
            ancien.noeud

          case _ =>
            log(s"[difference] discordance texte / noeud")
            val nouveauNoeud = dessiner(nouveauHtml)
            parent.replaceChild(nouveauNoeud, ancien.noeud)
            nouveauNoeud
        }
    }
  }
}
