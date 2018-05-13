package outils

sealed abstract class Diff[+A, +B]

object Diff {
  private val debug = false

  @inline
  private def log(s: => String): Unit =
    if (debug) println(s)

  // Log
  private implicit final class Log[A](self: A) {
    @inline
    def logCa(f: A => String): A = {
      log(f(self))
      self
    }
  }

  def logDiffs[A, B](l: List[Diff[A, B]]): String =
    l.map {
        case Ajout(b)    => s"+$b"
        case Suppr(a)    => s"-$a"
        case Ident(a, b) => s"$a=$b"
        case Rempl(a, b) => s"$a->$b"
      }
      .mkString(",")

  final case class Ajout[+B](valeur: B) extends Diff[Nothing, B]
  final case class Suppr[+A](valeur: A) extends Diff[A, Nothing]
  final case class Ident[+A, +B](de: A, vers: B) extends Diff[A, B]
  final case class Rempl[+A, +B](de: A, vers: B) extends Diff[A, B]

  @SuppressWarnings(
    Array("org.wartremover.warts.Var", "org.wartremover.warts.TraversableOps"))
  def apply[A, B](a1: List[A],
                  a2: List[B],
                  eq: (A, B) => Boolean): (Int, List[Diff[A, B]]) = {
    type Q = (Int, List[Diff[A, B]])
    type T = Array[Q]

    val n2 = a2.size
    val d = a2.zipWithIndex

    @scala.annotation.tailrec
    def auxI(l0: T, l1: T, lg: Seq[(A, Int)]): Q =
      lg match {
        case Nil =>
          val (c, l) = l0(n2)
          (c, l.reverse)

        case (c1, i) :: tl =>
          l1(0) = (i + 1, Suppr(c1) :: l0(0)._2)

          d.foreach {
            case (c2, j) =>
              def choix(p: Q, increment: Int, modif: Diff[A, B]): Q =
                (p._1 + increment, modif :: p._2)

              val l = List(
                choix(l0(j + 1), 1, Suppr(c1)),
                choix(l1(j), 1, Ajout(c2)), {
                  val p = l0(j)
                  val (inc, mod): (Int, Diff[A, B]) =
                    if (eq(c1, c2)) (0, Ident(c1, c2)) else (1, Rempl(c1, c2))
                  choix(p, inc, mod)
                }
              )
              l1(j + 1) = l.sortBy(_._1).head
          }

          auxI(l1, l0, tl)
      }

    val _l0: T = Array.fill(n2 + 1)((0, Nil))
    val _l1: T = Array.fill(n2 + 1)((0, Nil))

    def init_l0(j: Int)(l: List[Diff[A, B]]): List[Diff[A, B]] = {
      _l0(j) = (j, l)
      l
    }

    d.foldLeft { init_l0(0)(Nil) } {
      case (s, (c2, j)) => init_l0(j + 1)(Ajout(c2) :: s)
    }

    auxI(_l0, _l1, a1.zipWithIndex)
  }

  def myers[A, B](eq: (A, B) => Boolean)(
      l: Array[A],
      r: Array[B]): (Int, List[Diff[A, B]]) = {
    import scala.annotation._

    type D = List[Diff[A, B]]

    (l.size, r.size) match {
      case (0, nr)  => (nr, r.toList.map(Ajout(_)))
      case (nl, 0)  => (nl, l.toList.map(Suppr(_)))
      case (nl, nr) =>
        // Creation du Tableau V (avec indices de -vmax à +vmax, les deux inclus)
        object v {
          val vmax = nl + nr
          val _v: Array[(Int, D)] = Array.fill(2 * vmax + 1)((0, Nil: D))

          @inline def offset(i: Int): Int = i + vmax

          @inline def apply(k: Int): (Int, D) =
            _v(offset(k)).logCa(x =>
              s"    v.get($k)=${x._1},${x._1 - k} ${logDiffs(x._2)}")
          @inline def update(k: Int, a: (Int, D)): Unit =
            _v(offset(k)) = a.logCa(x =>
              s"    v.set($k)=${x._1},${x._1 - k} ${logDiffs(x._2)}")
        }

        // Initialisation
        v(1) = (0, Nil: D)

        @tailrec
        def auxD(d: Int): (Int, D) = {
          log(s":d=$d")
          def auxK(k: Int): Option[(Int, D)] =
            if (k > d)
              None
            else {
              log(s"  :k=$k")

              @inline
              def y(x: Int): Int = x - k

              @tailrec @inline
              def furthest(x: Int, p: D): (Int, D) = {
                if (x < nl && y(x) < nr && eq(l(x), r(y(x))))
                  furthest(x + 1, Ident(l(x), r(y(x))) :: p)
                else
                  (x, p)
              }

              val (x, p, suppr) =
                if (k == -d || ((k != d) && v(k - 1)._1 < v(k + 1)._1))
                  v(k + 1) match { case (_x, _p) => (_x, _p, false) } else
                  v(k - 1) match { case (_x, _p) => (_x + 1, _p, true) }

              log(
                s"    x,y=$x,${y(x)} ${if (suppr) "-/x" else "+/y"} ${logDiffs(p)}")

              if (x > nl || y(x) > nr) auxK(k + 2)
              else {

                val p2: D =
                  if (suppr)
                    if (x > 0) Suppr(l((x - 1))) :: p else p
                  else if (y(x) > 0) Ajout(r((y(x) - 1))) :: p
                  else p

                val fxp = furthest(x, p2)
                log(s"    fxp=${fxp._1},${y(fxp._1)} ${logDiffs(fxp._2)}")

                if (fxp._1 >= nl && y(fxp._1) >= nr)
                  Some((d, fxp._2.reverse))
                else {
                  v(k) = fxp
                  auxK(k + 2)
                }
              }
            }

          auxK(-d) match {
            case Some(ret) => ret
            case _         => auxD(d + 1)
          }
        }
        auxD(0)
    }
  }

  def myersChaines(s1: String, s2: String): (Int, List[Diff[Char, Char]]) =
    myers((x: Char, y: Char) => x === y)(s1.toArray, s2.toArray)
}
