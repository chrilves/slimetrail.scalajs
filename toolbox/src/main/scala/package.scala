package object toolbox:

  /** The operator {{{x == y}}} of Scala is dangerous because it allows {{{x}}} et {{{y}}} to be of
    * different types, which is often a bug.
    *
    * This implcit class replaces it by {{{x === y}}} where the two operands must have the same
    * type.
    */
  extension [A](self: A)
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    inline def ===(other: A): Boolean = self == other
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    inline def =/=(other: A): Boolean = self != other
