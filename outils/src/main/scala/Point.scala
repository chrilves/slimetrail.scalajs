package outils

final case class Point(x: Double, y: Double) {
  val real: Double = x
  val img: Double = y

  def +(p2: Point): Point = Point(x = this.x + p2.x, y = this.y + p2.y)
  def -(p2: Point): Point = Point(x = this.x - p2.x, y = this.y - p2.y)

  def vector(p2: Point): Point = p2 - this

  def *(p2: Point): Point =
    Point(
      x = (this.x * p2.x) - (this.y * p2.y),
      y = (this.x * p2.y) + (this.y * p2.x)
    )

  def rotate(angle: Double): Point = this * Point.polar(1.0, angle)

  def **(factor: Double): Point =
    Point(x = factor * this.x, y = factor * this.y)

  def scale(factor: Double): Point = this ** factor

  def conj: Point = Point(x = this.x, y = -this.y)

  override def toString = s"$x,$y"
}

object Point {
  import scala.math._

  def cart(x: Double, y: Double): Point = Point(x, y)

  def ei(angle: Double): Point = Point(x = cos(angle), y = sin(angle))

  def polar(rayon: Double, angle: Double): Point =
    Point(x = rayon * cos(angle), y = rayon * sin(angle))

  def deg2rat(angle: Double): Double = Pi * angle / 180.0

  def polarDeg(rayon: Double, angle: Double): Point =
    polar(rayon, deg2rat(angle))

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
