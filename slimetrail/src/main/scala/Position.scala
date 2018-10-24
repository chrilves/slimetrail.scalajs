package slimetrail

import toolbox._

/** Positions in a Slimetail hexagonal grid. */
final case class Position(up: Int, down: Int) {

  /** Neighbors of this position (not taking size into account). */
  def neighbors: Set[Position] =
    Set(
      copy(up = up - 1),
      copy(up = up + 1),
      copy(down = down - 1),
      copy(down = down + 1),
      copy(up = up + 1, down = down - 1),
      copy(up = up - 1, down = down + 1)
    ).filter(p => p.up >= 0 && p.down >= 0)

  /** Check whether two positions are neighbors in the grid. */
  def isNeighborOf(p2: Position): Boolean = {
    val diff_up = p2.up - this.up
    val diff_down = p2.down - this.down

    import scala.math.abs

    (abs(diff_up) === 1 && diff_down === 0) ||
    (abs(diff_down) === 1 && diff_up === 0) ||
    (diff_up === -diff_down && abs(diff_up) === 0)
  }
}
