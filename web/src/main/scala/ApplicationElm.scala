package slimetrail.web

import org.scalajs.dom.raw._
import slimetrail.web.html._

trait ApplicationElm { self =>
  private val debug = false

  @inline
  private def log(s: String): Unit = {
    if (debug) println(s)
    ()
  }

  type Model
  val modelInitial: Model
  def vue(model: Model): slimetrail.web.html.Html[Msg]

  type Msg
  def miseAJour(message: Msg, model: Model): Model

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final def executer(noeudInitial: Node, useDifference: Boolean): Unit = {
    import Html._

    final case class Etat(noeud: Node, vue: Html[Unit], model: Model)

    var etat: Etat =
      Etat(noeudInitial, Texte(""), modelInitial)

    def actualiser(nouveauModel: Model): Unit = {
      log(s"[ApplicationElm] nouveau modèle: $nouveauModel")
      val nouvelleVue: Html[Unit] =
        vue(nouveauModel).map { msg: Msg =>
          log(s"[ApplicationElm] nouveau message: $msg")
          actualiser(miseAJour(msg, etat.model))
        }

      val ancienNoeud: Node = etat.noeud
      val parent: Node = ancienNoeud.parentNode

      val nouveauNoeud: Node =
        if (useDifference)
          Rendu.difference(parent,
                           Rendu.Entree(etat.vue, etat.noeud),
                           nouvelleVue)
        else {
          val n = Rendu.dessiner(nouvelleVue)
          parent.replaceChild(n, ancienNoeud)
          n
        }

      etat = Etat(nouveauNoeud, nouvelleVue, nouveauModel)
    }

    actualiser(modelInitial)
  }
}
