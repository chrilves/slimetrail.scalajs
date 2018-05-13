package object outils {
  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit final class AnyOps[A](self: A) {
    @inline def ===(other: A): Boolean = self == other
    @inline def =/=(other: A): Boolean = self != other
  }
}
