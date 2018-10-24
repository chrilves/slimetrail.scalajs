package slimetrail

import toolbox._
import scala.annotation._

/** Implementation of an immutable hexagonal grid.
  * Positions {{Position(h,b)}} definied for a grid of size {{t}} are:
  *
  * `0 <= h < t`
  * `0 <= b < t`
  */
final class Hexa[+A](val size: Int, cells: Vector[A]) {
  private val debug: Boolean = false
  @inline private def log(s: => String): Unit =
    if (debug) println(s)

  private def offset(p: Position): Option[Int] =
    if (p.up >= 0 && p.up < size && p.down >= 0 && p.down < size)
      Some(p.up * size + p.down)
    else
      None

  private def position(i: Int): Position =
    Position(up = i / size, down = i % size)

  /** Get the value stored at that position*/
  def get(p: Position): Option[A] =
    offset(p).flatMap(cells.lift(_))

  /** Return a grid where the position {{position}} is {{value}}
    * (if this position is within the limits of the grid, otherwise return the input grid)
    */
  def set[B >: A](position: Position, value: B): Hexa[B] =
    offset(position) match {
      case Some(i) =>
        new Hexa[B](size, cells.updated(i, value))
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
         case (p, a) => s"  (${p.up},${p.down})\t-> $a"
       }.toVector.mkString("\n")}
    |}""".stripMargin

  /** Check whether two positions can be connected by a path made of cells for which the prediate holds*/
  def connectedBy(
      predicate: (Position, A) => Boolean,
      origin: Position,
      destination: Position
  ): Boolean = {

    @tailrec
    def rec(added: Set[Position], toVisit: List[Position]): Boolean = {
      log(
        s"[Hexa.atteignabePar] added.size=${added.size}, toVisit.taille=${toVisit.size}, toVisit=${toVisit.take(5).mkString(",")}")
      toVisit match {
        case Nil =>
          false

        case hd :: tl =>
          if (hd === destination)
            true
          else {
            val next =
              hd.neighbors
                .filter { p2 =>
                  get(p2) match {
                    case Some(a) =>
                      log(s"[Hexa.atteignabePar] grille[$p2]=$a")
                      !added.contains(p2) && predicate(p2, a)
                    case None =>
                      log(s"[Hexa.atteignabePar] grille[$p2] n'existe pas.")
                      false
                  }
                }

            rec(added ++ next, next.toList ++ tl)
          }
      }
    }

    rec(Set(origin), List(origin))
  }

  /** Compure reachable positions from the one given as input by paths satisfying the predicate*/
  def reachableFrom(
      origin: Position,
      predicate: (Position, A) => Boolean
  ): Set[Position] = {

    @tailrec
    def rec(added: Set[Position], toVisit: List[Position]): Set[Position] = {
      log(
        s"[Hexa.atteignabeDepuis] added.size=${added.size}, toVisit.size=${toVisit.size}, toVisit=${toVisit.take(5).mkString(",")}")
      toVisit match {
        case Nil =>
          added

        case hd :: tl =>
          val next =
            hd.neighbors
              .filter { p2 =>
                get(p2) match {
                  case Some(a) =>
                    log(s"[Hexa.atteignabeDepuis] grille[$p2]=$a")
                    !added.contains(p2) && predicate(p2, a)
                  case None =>
                    log(s"[Hexa.atteignabeDepuis] grille[$p2] n'existe pas.")
                    false
                }
              }

          rec(added ++ next, next.toList ++ tl)
      }
    }

    rec(Set(origin), List(origin))
  }
}

object Hexa {

  /** Create an immutable square hexagonal grid of size {{size}} filled with {{value}}*/
  def fill[A](_size: Int)(value: A): Hexa[A] =
    new Hexa[A](_size, Vector.fill(_size * _size)(value))

  /** Create an immutable square hexagonal grid of size {{size}} filled by {{f}}*/
  def tabulate[A](_size: Int)(f: Position => A): Hexa[A] =
    new Hexa[A](_size, Vector.tabulate(_size * _size) { i =>
      f(Position(up = i / _size, down = i % _size))
    })
}
