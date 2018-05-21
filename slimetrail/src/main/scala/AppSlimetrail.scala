package slimetrail

/** Squelette d'une application Slimetrail. L'interface n'est pas ici
  * précisée mais l'état de l'application est une partie et les
  * évènements sont les coups joués (et nouvelle partie).
  */
abstract class AppSlimetrail(taille: Int) extends ApplicationElm {

  /** Une application Slimetrail, n'a à chaque instant besoin que de l'état
    * de la partie pour faire son office
    */
  final type Model = Partie

  /** Pour faire avancer la partie, soit un coup est joué, soit
    * une nouvelle partie démarre.
    */
  sealed abstract class Msg
  final case class UnCoup(coup: Coup) extends Msg
  final case object NouvellePartie extends Msg

  /** L'état initial d'une partie (donc de l'application) */
  final lazy val modelInitial: Partie = Partie.debut(taille)

  /** Avancement de l'état d'une partie (donc de l'application) en réponse
    * aux évènements possibles: nouveau coup ou nouvelle partie.
    */
  final def miseAJour(msg: Msg, model: Partie): Partie =
    msg match {
      case UnCoup(c)      => model.jouerUnCoup(c).getOrElse(model)
      case NouvellePartie => Partie.debut(taille)
    }
}
