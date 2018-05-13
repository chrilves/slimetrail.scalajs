package slimetrail

import outils._
import scala.annotation._

final class Hexa[+A](val size: Int, cells: Vector[A]) {
  private val debug: Boolean = false
  @inline private def log(s: => String): Unit =
    if (debug) println(s)

  private def offset(p: Position): Option[Int] =
    if (p.haut >= 0 && p.haut < size && p.bas >= 0 && p.bas < size)
      Some(p.haut * size + p.bas)
    else
      None

  private def position(i: Int): Position =
    Position(haut = i / size, bas = i % size)

  def get(p: Position): Option[A] =
    offset(p).flatMap(cells.lift(_))

  def set[B >: A](position: Position, valeur: B): Hexa[B] =
    offset(position) match {
      case Some(i) =>
        new Hexa[B](size, cells.updated(i, valeur))
      case _ => this
    }

  def map[B](f: A => B): Hexa[B] =
    new Hexa[B](size, cells.map(f))

  def indexedMap[B](f: (Position, A) => B): Hexa[B] = {
    val builder = Vector.newBuilder[B]
    for (i <- 0 to cells.size - 1) {
      builder += f(position(i), cells(i))
    }
    new Hexa[B](size, builder.result)
  }

  def toVector: Vector[A] = cells

  override def toString =
    s"""{
    |${indexedMap {
         case (p, a) => s"  (${p.haut},${p.bas})\t-> $a"
       }.toVector.mkString("\n")}
    |}""".stripMargin

  def connecteesPar(
      predicat: (Position, A) => Boolean,
      origine: Position,
      destination: Position
  ): Boolean = {

    @tailrec
    def rec(added: Set[Position], toVisit: List[Position]): Boolean = {
      log(
        s"[Hexa.atteignabePar] added.size=${added.size}, toVisit.size=${toVisit.size}, toVisit=${toVisit.take(5).mkString(",")}")
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

  def atteignableDepuis(
      origine: Position,
      predicat: (Position, A) => Boolean
  ): Set[Position] = {

    @tailrec
    def rec(added: Set[Position], toVisit: List[Position]): Set[Position] = {
      log(
        s"[Hexa.atteignabeDepuis] added.size=${added.size}, toVisit.size=${toVisit.size}, toVisit=${toVisit.take(5).mkString(",")}")
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
  def fill[A](_size: Int)(value: A): Hexa[A] =
    new Hexa[A](_size, Vector.fill(_size * _size)(value))

  def tabulate[A](_size: Int)(f: Position => A): Hexa[A] =
    new Hexa[A](_size, Vector.tabulate(_size * _size) { i =>
      f(Position(haut = i / _size, bas = i % _size))
    })
}
