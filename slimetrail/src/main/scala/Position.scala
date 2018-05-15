package slimetrail

import outils._

/** Les positions dans une grille hexagonale de Slimetail.*/
final case class Position(haut: Int, bas: Int) {

  /** Les cases voisinnes de cette position (sans limite de taille).*/
  def voisinnes: Set[Position] =
    Set(
      copy(haut = haut - 1),
      copy(haut = haut + 1),
      copy(bas = bas - 1),
      copy(bas = bas + 1),
      copy(haut = haut + 1, bas = bas - 1),
      copy(haut = haut - 1, bas = bas + 1)
    ).filter(p => p.haut >= 0 && p.bas >= 0)

  /** Détermine si les deux positions sont adjacentes dans la grille.*/
  def estAdjacenteDe(p2: Position): Boolean = {
    val diff_haut = p2.haut - this.haut
    val diff_bas = p2.bas - this.bas

    import scala.math.abs

    (abs(diff_haut) === 1 && diff_bas === 0) ||
    (abs(diff_bas) === 1 && diff_haut === 0) ||
    (diff_haut === -diff_bas && abs(diff_haut) === 0)
  }
}
