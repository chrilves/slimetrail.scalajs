package slimetrail

abstract class AppSlimetrail(taille: Int) {
  final type Model = Partie

  sealed abstract class Msg
  final case class UnCoup(coup: Coup) extends Msg
  final case object NouvellePartie extends Msg

  final val modelInitial: Partie = Partie.debut(taille)

  final def miseAJour(msg: Msg, model: Partie): Partie =
    msg match {
      case UnCoup(c)      => model.jouerUnCoup(c, model).getOrElse(model)
      case NouvellePartie => Partie.debut(taille)
    }
}
