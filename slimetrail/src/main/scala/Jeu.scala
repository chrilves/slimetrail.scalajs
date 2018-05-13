package slimetrail

import outils._

/** C'est un jeu à deux joueurs.euses.
  * Il se joue tour par tour. Il y a donc
  * un.e premièr.e joueur.euse et un.e
  * second.e jouer.euse.
  */
sealed abstract class Joueur {
  import Joueur._

  def suivant: Joueur =
    this match {
      case Premier => Second
      case Second  => Premier
    }
}
object Joueur {
  case object Premier extends Joueur
  case object Second extends Joueur
}

/** Représente l'état de la partie.
  * Soit la partie à été gagnée par un.e
  * des deux joeurs, soit c'est à un.e
  * joueur.euse de jouer.
  */
sealed abstract class Tour { val joueur: Joueur }
object Tour {
  final case class Au(joueur: Joueur) extends Tour
  final case class GagnePar(joueur: Joueur) extends Tour
}

/** Représente un coup, fait par un.e jouer.euse
  * sur une nouvelle position.
  */
final case class Coup(position: Position)

/** Une case de la grille */
final case class Case(visitee: Boolean)

sealed abstract class Partie {
  val grille: Hexa[Case]
  val positionActuelle: Position
  val tour: Tour
  val historique: Vector[Coup]

  final def enCours: Boolean =
    tour match {
      case Tour.GagnePar(_) => false
      case Tour.Au(_)       => true
    }

  final def coupsValides: Map[Position, Option[Joueur]] = {
    import Partie._
    import Joueur._

    def positionsAtteignables(j: Joueur): Set[Position] =
      grille.atteignableDepuis(
        objectif(j),
        (p2: Position, c: Case) =>
          !c.visitee && p2 =/= objectif(j.suivant) && p2 =/= positionActuelle
      )

    val positionsPremier = positionsAtteignables(Premier)
    val positionsSecond = positionsAtteignables(Second)

    def atteignables(j: Joueur): Set[Position] =
      j match {
        case Premier => positionsPremier
        case Second  => positionsSecond
      }

    val m: List[(Position, Option[Joueur])] =
      for {
        p <- positionActuelle.voisinnes.toList if enCours
        c <- grille.get(p).toList if !c.visitee
        g <- List[Joueur](Premier, Second)
          .filter(j => atteignables(j).contains(p)) match {
          case Nil     => Nil
          case List(j) => List(Some(j))
          case _       => List(None)
        }
      } yield (p, g)

    m.toMap
  }

  final def jouerUnCoup(c: Coup, p: Partie): Option[Partie] =
    p.tour match {
      case Tour.GagnePar(_) =>
        None

      case Tour.Au(j) =>
        val nouvelleGrille = p.grille.set(p.positionActuelle, Case(true))

        def nouvellePartie(t: Tour): Partie =
          new Partie {
            val grille: Hexa[Case] = nouvelleGrille
            val positionActuelle: Position = c.position
            val tour: Tour = t
            val historique: Vector[Coup] = p.historique.:+(c)
          }

        coupsValides.get(c.position).map { _ =>
          Partie.positionGagnante(c.position) match {
            case Some(j) => nouvellePartie(Tour.GagnePar(j))
            case _       => nouvellePartie(Tour.Au(j.suivant))
          }
        }
    }

  override def toString: String =
    s"""
    |====== Partie de Slimetrail ===========
    |  historique=$historique
    |
    |  tour=$tour
    |
    |  position=$positionActuelle
    |
    |  grille=$grille
    |
    |---------------------------------------
    |""".stripMargin
}

object Partie {
  val debut: Partie = new Partie {
    val grille: Hexa[Case] = Hexa.fill(10)(Case(false))
    val positionActuelle: Position = Position(4, 5)
    val tour: Tour = Tour.Au(Joueur.Premier)
    val historique: Vector[Coup] = Vector.empty
  }

  val objectifPremierJoueur: Position = Position(9, 0)
  val objectifSecondJoueur: Position = Position(0, 9)

  def objectif(j: Joueur): Position =
    j match {
      case Joueur.Premier => objectifPremierJoueur
      case Joueur.Second  => objectifSecondJoueur
    }

  def positionGagnante(p: Position): Option[Joueur] =
    p match {
      case `objectifPremierJoueur` => Some(Joueur.Premier)
      case `objectifSecondJoueur`  => Some(Joueur.Second)
      case _                       => None
    }
}
