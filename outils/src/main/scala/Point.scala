package outils

/** Implémentation de Points en 2D (c'est à dire des nombres complexes) */
final case class Point(x: Double, y: Double) {

  /**Axe des absices X du point/Partie réèle du nombre complexe*/
  val real: Double = x

  /** Axe des ordonnés Y du point/Partie imaginaire du nombre complexe*/
  val img: Double = y

  /** Addition composantes à composante de deux points/ Addition des nombres complexes
    * {{(x = p1.x + p2.x, y = p1.y + p2.y)}}
    */
  def +(p2: Point): Point = Point(x = this.x + p2.x, y = this.y + p2.y)

  /** Soustraction composantes à composante de deux points/ Soustraction des nombres complexes
    * {{(x = p1.x - p2.x, y = p1.y - p2.y)}}
    */
  def -(p2: Point): Point = Point(x = this.x - p2.x, y = this.y - p2.y)

  /** Le vecteur menant du point {{this}} à {{p2}}
    */
  def vector(p2: Point): Point = p2 - this

  /** Multiplication de nombres complexes
    * Exprimés en coordonnées polaire, avec le point p1 de rayon r1 et angle a1.
    * Et le point p2 de rayon r2 et angle a2. Le résultat est de rayon r1*r2
    * et d'angle a1+a2.
    */
  def *(p2: Point): Point =
    Point(
      x = (this.x * p2.x) - (this.y * p2.y),
      y = (this.x * p2.y) + (this.y * p2.x)
    )

  /** Rotation du point par un angle en radian (et de centre l'origine).*/
  def rotate(angle: Double): Point = this * Point.polar(1.0, angle)

  /** Multilication des composantes X et Y par un facteur/ Multipication du nombre complexe par un réèl.*/
  def **(factor: Double): Point =
    Point(x = factor * this.x, y = factor * this.y)

  /** Multilication des composantes X et Y par un facteur/ Multipication du nombre complexe par un réèl.*/
  def scale(factor: Double): Point = this ** factor

  /** Conjugé du complexe*/
  def conj: Point = Point(x = this.x, y = -this.y)

  override def toString = s"$x,$y"
}

object Point {
  import scala.math._

  /** Définit le point en corrdonnées catésiennes (X,Y)*/
  def cart(x: Double, y: Double): Point = Point(x, y)

  /** Définit le point en polaires (rayan,angle)*/
  def polar(rayon: Double, angle: Double): Point =
    Point(x = rayon * cos(angle), y = rayon * sin(angle))

  /** Point à distance 1 de l'origine et d'angle {{angle}}*/
  def ei(angle: Double): Point = Point(x = cos(angle), y = sin(angle))

  /** Conversion de degrés en radians */
  def deg2rat(angle: Double): Double = Pi * angle / 180.0

  /** Définit le point en polaires (rayan,angle) avec angle en radian.*/
  def polarDeg(rayon: Double, angle: Double): Point =
    polar(rayon, deg2rat(angle))

  /** Les 6 points que forme un hexagone de rayon 1 et de centre l'origine.
    * Ils sont dans l'ordre d'angle croissant.
    */
  val hexagonRayon1Centre0: List[Point] =
    List(
      Point.polarDeg(1, 0 * 60),
      Point.polarDeg(1, 1 * 60),
      Point.polarDeg(1, 2 * 60),
      Point.polarDeg(1, 3 * 60),
      Point.polarDeg(1, 4 * 60),
      Point.polarDeg(1, 5 * 60)
    )
}
