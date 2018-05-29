package slimetrail.web

import org.scalajs.dom.raw._
import slimetrail.web.html._
import slimetrail._

/** Une application Web "à la ELM"
  * C'est comme une application texte, sauf que la vue renvoie un arbre Html
  */
trait ApplicationWebElm extends ApplicationElm { self =>

  /** La vue renvoie un arbre Html*/
  def vue(model: Model): slimetrail.web.html.Html[Msg]

  /** Fait tourner une application Web à la Elm sur un noeud donné*
    *
    * Le principe est simple:
    *  - Comme toute [[ApplicationElm]], il y a à chaque instant un
    *    "modèle courrant" qui est une valeur de type [[Model]].
    *  - La fonction [[vue]] calcule l'arbre Html correspondant à la
    *    valeur actuelle du modèle.
    *  - Cet arbre permet de construire un noeud du Dom
    *  - Ce nouveau noeud remplace l'ancien.
    *  - A chaque évènement, donc à chaque réaction exécuté,
    *    la valeur de retour de cette réaction est un message
    *    (de type [[Msg]]).
    *  - Ce message donne lieu à un nouveau modèle via la fonction
    *    [[miseAJour]].
    *  - Ce modèlé devient le nouveau modèle courrant, et on recommence.
    */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final def executer(noeudInitial: Node): Unit = {
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

      val nouveauNoeud: Node = {
        val n = nouvelleVue.dessiner
        parent.replaceChild(n, ancienNoeud)
        n
      }

      etat = Etat(nouveauNoeud, nouvelleVue, nouveauModel)
    }

    actualiser(modelInitial)
  }
}
