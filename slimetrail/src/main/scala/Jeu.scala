package slimetrail

import toolbox._

/** It's a turn-based two-player game.
  * There is so a first and second player.
  */
sealed abstract class Player {
  import Player._

  def next: Player =
    this match {
      case First  => Second
      case Second => First
    }
}

object Player {
  case object First extends Player
  case object Second extends Player
}

/** Represents whether a game has been won
  * and by who or is sill in play and
  * whose turn it is.
  */
sealed abstract class Turn { val player: Player }
object Turn {
  final case class TurnTo(player: Player) extends Turn
  final case class WonBy(player: Player) extends Turn
}

/** Represents a move on a new position.
  */
final case class Move(position: Position)

/** A cell of the grid */
final case class Cell(visited: Boolean)

/** Represents the state of a game of Slimetrail */
sealed abstract class GameState { self =>
  val grid: Hexa[Cell]
  val currentPosition: Position
  val turn: Turn

  /** Past moves. Index 0 is the first move. */
  val history: Vector[Move]

  final lazy val size: Int = grid.size

  /** The position the first player aims to reach */
  final lazy val firstPlayerGoal: Position = Position(size - 1, 0)

  /** The position the second player aims to reach */
  final lazy val secondPlayerGoal: Position = Position(0, size - 1)

  final lazy val startingPosition: Position = GameState.startingPosition(size)

  /** The width of the grid (with hexagons cells of radius 1)*/
  final lazy val width: Double = 3 * size.toDouble - 1

  /** The half the of the heigh of the grid (with hexagons of radius 1)*/
  final lazy val halfHeigh: Double = size * math.sin(math.Pi * 60 / 180)

  final def goal(j: Player): Position =
    j match {
      case Player.First  => firstPlayerGoal
      case Player.Second => secondPlayerGoal
    }

  /** Tells if the position is a victory position of a player */
  final def winningPosition(p: Position): Option[Player] =
    p match {
      case `firstPlayerGoal`  => Some(Player.First)
      case `secondPlayerGoal` => Some(Player.Second)
      case _                  => None
    }

  /** Is the game still running ? */
  final def onGoing: Boolean =
    turn match {
      case Turn.WonBy(_)  => false
      case Turn.TurnTo(_) => true
    }

  /** Only some moves are legal depending in the game state */
  final def allowedMoves: Map[Position, Option[Player]] = {
    import Player._

    def reacheablePositions(j: Player): Set[Position] =
      grid.reachableFrom(
        goal(j),
        (p2: Position, c: Cell) =>
          !c.visited && p2 =/= goal(j.next) && p2 =/= currentPosition
      )

    val positionsFirst = reacheablePositions(First)
    val positionsSecond = reacheablePositions(Second)

    def reachable(j: Player): Set[Position] =
      j match {
        case First  => positionsFirst
        case Second => positionsSecond
      }

    val m: List[(Position, Option[Player])] =
      for {
        p <- currentPosition.neighbors.toList if onGoing
        c <- grid.get(p).toList if !c.visited
        g <- List[Player](First, Second)
          .filter(j => reachable(j).contains(p)) match {
          case Nil     => Nil
          case List(j) => List(Some(j))
          case _       => List(None)
        }
      } yield (p, g)

    m.toMap
  }

  /** Play a move. If the last one is allowed, return the new game state.
    * Otherwise return {{[None}}}.
    */
  final def playAMove(c: Move): Option[GameState] =
    turn match {
      case Turn.WonBy(_) =>
        None

      case Turn.TurnTo(j) =>
        val newGrid = grid.set(currentPosition, Cell(true))

        def newGame(t: Turn): GameState =
          new GameState {
            val grid: Hexa[Cell] = newGrid
            val currentPosition: Position = c.position
            val turn: Turn = t
            val history: Vector[Move] = self.history.:+(c)
          }

        allowedMoves.get(c.position).map { _ =>
          winningPosition(c.position) match {
            case Some(j) => newGame(Turn.WonBy(j))
            case _       => newGame(Turn.TurnTo(j.next))
          }
        }
    }

  override def toString: String =
    s"""
    |====== Partie de Slimetrail ===========
    |  history=${history.toString}
    |
    |  turn=${turn.toString}
    |
    |  position=${currentPosition.toString}
    |
    |  grid=${grid.toString}
    |
    |---------------------------------------
    |""".stripMargin
}

object GameState {
  def startingPosition(taille: Int): Position = {
    val max = taille - 1
    val mid = max / 2
    Position(mid, max - mid)
  }

  /** Initial state of a game for a given size */
  def initial(size: Int): GameState = {
    val theSize = math.max(size, 3)

    new GameState {
      val grid: Hexa[Cell] = Hexa.fill(theSize)(Cell(false))
      val currentPosition: Position = GameState.startingPosition(size)
      val turn: Turn = Turn.TurnTo(Player.First)
      val history: Vector[Move] = Vector.empty
    }
  }

  /** The translation vector from an hexagon of the grid at Position(X,Y) to one at Position(X,Y+1) */
  val up: Point = Point.polarDeg(math.sqrt(3), 30)

  /** The translation vector from an hexagon of the grid at Position(X,Y) to one at Position(X+1,Y) */
  val down: Point = Point.polarDeg(math.sqrt(3), -30)

  /** Give the point corresponding to the center of the hexagon at the given position. */
  def coordinates(pos: Position): Point =
    (up ** pos.up.toDouble) + (down ** pos.down.toDouble)
}
