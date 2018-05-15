package slimetrail.texte

import scala.annotation._
import slimetrail._

/** Spécialisation d'une [[ApplicationElm]] à une interface texte.*/
trait ApplicationTexteElm extends ApplicationElm { self =>

  /** La vue à pour but d'afficher l'état actuel de l'application, donné
    * par {{model}} et rendre le message [[Msg]] permettant de passer
    * à l'état suivant.
    */
  def vue(model: Model): Msg

  /** Exécute une application.*/
  final def executer(): Unit = {
    @tailrec
    def boucle(model: Model): Unit = {
      val msg: Msg = vue(model)
      val modelSuivant: Model = miseAJour(msg, model)
      boucle(modelSuivant)
    }

    boucle(modelInitial)
  }
}
