package object toolbox {

  /** L'opérateur {{x == y}} de Scala est dangereux du fait qu'il autorise {{x}} et {{y}}
    * à être de types différent, ce qui est souvent le signe d'un bug.
    *
    * Cette classe implicite le remplace par {{{x === y}}} où les deux opérandes doivent
    * avoir le même type.
    */
  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit final class AnyOps[A](self: A) {
    @inline def ===(other: A): Boolean = self == other
    @inline def =/=(other: A): Boolean = self != other
  }
}
