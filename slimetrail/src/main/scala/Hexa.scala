package slimetrail

import outils._
import scala.annotation._

/** Implémentation d'une grille carrée hexagonale immutable.
  * Les positions {{Position(h,b)}} définies pour une grille de taille {{t}} sont:
  *
  * `0 <= h < t`
  * `0 <= b < t`
  */
final class Hexa[+A](val taille: Int, cells: Vector[A]) {
  private val debug: Boolean = false
  @inline private def log(s: => String): Unit =
    if (debug) println(s)

  private def offset(p: Position): Option[Int] =
    if (p.haut >= 0 && p.haut < taille && p.bas >= 0 && p.bas < taille)
      Some(p.haut * taille + p.bas)
    else
      None

  private def position(i: Int): Position =
    Position(haut = i / taille, bas = i % taille)

  /** Récupère la valeur stockée dans une position */
  def get(p: Position): Option[A] =
    offset(p).flatMap(cells.lift(_))

  /** Renvoie une grille ou la position {{position}} vaut {{valeur}}
    * (si cette position est dans les limites de la grille, sinon renvoie la grille d'origine)
    */
  def set[B >: A](position: Position, valeur: B): Hexa[B] =
    offset(position) match {
      case Some(i) =>
        new Hexa[B](taille, cells.updated(i, valeur))
      case _ => this
    }

  def map[B](f: A => B): Hexa[B] =
    new Hexa[B](taille, cells.map(f))

  def indexedMap[B](f: (Position, A) => B): Hexa[B] = {
    val builder = Vector.newBuilder[B]
    for (i <- 0 to cells.size - 1) {
      builder += f(position(i), cells(i))
    }
    new Hexa[B](taille, builder.result)
  }

  def toVector: Vector[A] = cells

  override def toString =
    s"""{
    |${indexedMap {
         case (p, a) => s"  (${p.haut},${p.bas})\t-> $a"
       }.toVector.mkString("\n")}
    |}""".stripMargin

  /** Calcule si les deux positions peuvent être reliés par des cases respectant le prédicat */
  def connecteesPar(
      predicat: (Position, A) => Boolean,
      origine: Position,
      destination: Position
  ): Boolean = {

    @tailrec
    def rec(added: Set[Position], toVisit: List[Position]): Boolean = {
      log(
        s"[Hexa.atteignabePar] added.size=${added.size}, toVisit.taille=${toVisit.size}, toVisit=${toVisit.take(5).mkString(",")}"
      )
      toVisit match {
        case Nil =>
          false

        case hd :: tl =>
          if (hd === destination)
            true
          else {
            val suivantes =
              hd.voisinnes
                .filter { p2 =>
                  get(p2) match {
                    case Some(a) =>
                      log(s"[Hexa.atteignabePar] grille[$p2]=$a")
                      !added.contains(p2) && predicat(p2, a)
                    case None =>
                      log(s"[Hexa.atteignabePar] grille[$p2] n'existe pas.")
                      false
                  }
                }

            rec(added ++ suivantes, suivantes.toList ++ tl)
          }
      }
    }

    rec(Set(origine), List(origine))
  }

  /** Calcule les positions atteignables depuis la position donnée en respectant le prédicat*/
  def atteignableDepuis(
      origine: Position,
      predicat: (Position, A) => Boolean
  ): Set[Position] = {

    @tailrec
    def rec(added: Set[Position], toVisit: List[Position]): Set[Position] = {
      log(
        s"[Hexa.atteignabeDepuis] added.size=${added.size}, toVisit.size=${toVisit.size}, toVisit=${toVisit.take(5).mkString(",")}"
      )
      toVisit match {
        case Nil =>
          added

        case hd :: tl =>
          val suivantes =
            hd.voisinnes
              .filter { p2 =>
                get(p2) match {
                  case Some(a) =>
                    log(s"[Hexa.atteignabeDepuis] grille[$p2]=$a")
                    !added.contains(p2) && predicat(p2, a)
                  case None =>
                    log(s"[Hexa.atteignabeDepuis] grille[$p2] n'existe pas.")
                    false
                }
              }

          rec(added ++ suivantes, suivantes.toList ++ tl)
      }
    }

    rec(Set(origine), List(origine))
  }
}

object Hexa {

  /** Crée une grille hexagonale carrée immutable de taille {{taille}} remplie de {{value}}*/
  def fill[A](_taille: Int)(value: A): Hexa[A] =
    new Hexa[A](_taille, Vector.fill(_taille * _taille)(value))

  /** Crée une grille hexagonale carrée immutable de taille {{taille}} remplie par {{f}}*/
  def tabulate[A](_taille: Int)(f: Position => A): Hexa[A] =
    new Hexa[A](_taille, Vector.tabulate(_taille * _taille) { i =>
      f(Position(haut = i / _taille, bas = i % _taille))
    })
}
