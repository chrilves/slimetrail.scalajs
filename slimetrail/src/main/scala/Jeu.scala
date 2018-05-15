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

/** Réprésente l'état d'une partie de Slimetrail */
sealed abstract class Partie {
  val grille: Hexa[Case]
  val positionActuelle: Position
  val tour: Tour

  /** La séquence des coups passés. L'indice 0 est le premier coup. */
  val historique: Vector[Coup]

  final lazy val taille: Int = grille.taille

  /** La position que le premier joueur doit atteindre */
  final lazy val objectifPremierJoueur: Position = Position(taille - 1, 0)

  /** La position que le second joueur doit atteindre */
  final lazy val objectifSecondJoueur: Position = Position(0, taille - 1)
  final lazy val positionDeDepart: Position = Partie.positionDeDepart(taille)

  /** La largeur de la grille avec des hexagones de rayon 1*/
  final lazy val largeur: Double = 3 * taille.toDouble - 1

  /** La moitié de la hauteur de la grille avec des hexagones de rayon 1*/
  final lazy val demiHauteur: Double = taille * math.sin(math.Pi * 60 / 180)

  final def objectif(j: Joueur): Position =
    j match {
      case Joueur.Premier => objectifPremierJoueur
      case Joueur.Second  => objectifSecondJoueur
    }

  /** Indique si la position est une de victoire pour l'un des joueur */
  final def positionGagnante(p: Position): Option[Joueur] =
    p match {
      case `objectifPremierJoueur` => Some(Joueur.Premier)
      case `objectifSecondJoueur`  => Some(Joueur.Second)
      case _                       => None
    }

  /** La partie est en cours ou non*/
  final def enCours: Boolean =
    tour match {
      case Tour.GagnePar(_) => false
      case Tour.Au(_)       => true
    }

  /** Seuls certains coups sont possibles suivant l'état de la partie*/
  final def coupsValides: Map[Position, Option[Joueur]] = {
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

  /** Joue un coup. Si ce dernier est valide, renvoie le nouvel état de la partie.
    * Sinon, renvoie {{None}}.
    */
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
          positionGagnante(c.position) match {
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
  def positionDeDepart(taille: Int): Position = {
    val max = taille - 1
    val mid = max / 2
    Position(mid, max - mid)
  }

  /** Etat initial d'une partie de taille donnée */
  def debut(taille_ : Int): Partie = {
    val lataille = math.max(taille_, 3)

    new Partie {
      val grille: Hexa[Case] = Hexa.fill(lataille)(Case(false))
      val positionActuelle: Position = Partie.positionDeDepart(taille)
      val tour: Tour = Tour.Au(Joueur.Premier)
      val historique: Vector[Coup] = Vector.empty
    }
  }

  /** Le vecteur de translation pour passer d'un hexagone de la grille de
    * Position(X,Y) à Position(X,Y+1)
    */
  val haut: Point = Point.polarDeg(math.sqrt(3), 30)

  /** Le vecteur de translation pour passer d'un hexagone de la grille de
    * Position(X,Y) à Position(X+1,Y)
    */
  val bas: Point = Point.polarDeg(math.sqrt(3), -30)

  /** Donne le point correspondant au centre de l'hexagone à la position donnée.*/
  def coordonnees(pos: Position): Point =
    (haut ** pos.haut.toDouble) + (bas ** pos.bas.toDouble)
}
