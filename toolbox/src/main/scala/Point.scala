package toolbox

/** Implementation of 2D Points (i.e. complex numbers) */
final case class Point(x: Double, y: Double) {

  /** X axis of the the point/real part of the complex number*/
  val real: Double = x

  /** Y axis of the point/imaginary part of the complex number*/
  val img: Double = y

  /** Addition compenent wise of two points/ Addition of complex numbers
    * {{{(x = p1.x + p2.x, y = p1.y + p2.y)}}}
    */
  def +(p2: Point): Point = Point(x = this.x + p2.x, y = this.y + p2.y)

  /** Substraction component wise of two points/ Substraction of complex numbers
    * {{{(x = p1.x - p2.x, y = p1.y - p2.y)}}}
    */
  def -(p2: Point): Point = Point(x = this.x - p2.x, y = this.y - p2.y)

  /** Vector from {{{this}}} to {{{p2}}}
    */
  def vector(p2: Point): Point = p2 - this

  /** Multiplication of complex numbers
    * In polar coordinates, with point p1 of radius r1 and angle a1.
    * And point p2 of radius r2 and angle a2. The result is of radius r1*r2
    * and angle a1+a2.
    */
  def *(p2: Point): Point =
    Point(
      x = (this.x * p2.x) - (this.y * p2.y),
      y = (this.x * p2.y) + (this.y * p2.x)
    )

  /** Rotation of a point by an angle in radian (the center is the origin).*/
  def rotate(angle: Double): Point = this * Point.polar(1.0, angle)

  /** Multilication of X and Y components by a scalar/ Multipication of a complex number by a real.*/
  def **(factor: Double): Point =
    Point(x = factor * this.x, y = factor * this.y)

  /** Multilication of X and Y components by a scalar/ Multipication of a complex number by a real.*/
  def scale(factor: Double): Point = this ** factor

  /** Conjugate of a complex number*/
  def conj: Point = Point(x = this.x, y = -this.y)

  override def toString = s"$x,$y"
}

object Point {
  import scala.math._

  /** Define the point in cartasian coordinates (X,Y)*/
  def cart(x: Double, y: Double): Point = Point(x, y)

  /** Define the point in polar coordinates (radius,angle)*/
  def polar(rayon: Double, angle: Double): Point =
    Point(x = rayon * cos(angle), y = rayon * sin(angle))

  /** Point at a distance of 1 from the origin and at angle {{{angle}}} */
  def ei(angle: Double): Point = Point(x = cos(angle), y = sin(angle))

  /** Conversion from degres to radians*/
  def deg2rat(angle: Double): Double = Pi * angle / 180.0

  /** Define the point in polar cordinates (radius,angle) with angle in radian.*/
  def polarDeg(rayon: Double, angle: Double): Point =
    polar(rayon, deg2rat(angle))

  /** The 6 points of an regular hexagon of radius 1 centered at the origin.
    * Ordered by increasing angle.
    */
  val hexagonRadius1AtOrigin: List[Point] =
    List(
      Point.polarDeg(1, 0 * 60),
      Point.polarDeg(1, 1 * 60),
      Point.polarDeg(1, 2 * 60),
      Point.polarDeg(1, 3 * 60),
      Point.polarDeg(1, 4 * 60),
      Point.polarDeg(1, 5 * 60)
    )
}
