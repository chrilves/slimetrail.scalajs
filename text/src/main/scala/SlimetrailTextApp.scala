package slimetrail.text

import slimetrail._

/** Text interface of a Slimetrail application .*/
final class SlimetrailTextApp(size: Int)
    extends SlimetrailApp(size)
    with TextApplication {

  /** Print a game state and produces events .*/
  def view(m: GameState): Msg = {
    log(s"[Slimetrail] Computing the view")

    val allowedMoves: Set[Position] = m.allowedMoves.keySet

    val colorOfAllowedMoves: String =
      m.turn.player match {
        case Player.First  => s"${Console.GREEN}C${Console.RESET}"
        case Player.Second => s"${Console.YELLOW}C${Console.RESET}"
      }

    val colorOfCellMoves: Map[Position, String] =
      allowedMoves.map((_, colorOfAllowedMoves)).toMap ++
        Map(
          m.firstPlayerGoal -> s"${Console.GREEN}F${Console.RESET}",
          m.secondPlayerGoal -> s"${Console.YELLOW}S${Console.RESET}",
          m.currentPosition -> s"${Console.RED}A${Console.RESET}"
        )

    val hexagons: Hexa[String] =
      m.grid
        .indexedMap {
          case (pos, Cell(visited)) =>
            if (visited)
              "X"
            else
              colorOfCellMoves.getOrElse(pos, " ")
        }

    val indices: List[Int] =
      (0 to (m.size - 1)).toList

    val hline: String = ("-" * (3 * (m.size + 1) + 1)) ++ "\n"

    val header: String =
      "   -" ++
        ("-" * (3 * m.size)) ++
        "\n   |" ++
        indices.map { i =>
          s"$i".padTo(2, ' ') ++ "|"
        }.mkString ++
        "\n" ++
        hline

    def line(i: Int): String =
      "|" ++ s"$i".padTo(2, ' ') ++ "|" ++
        (for {
          j <- indices
          s <- hexagons.get(Position(i, j)).toList
        } yield s"$s |").mkString ++
        "\n" ++
        hline

    val grid: String =
      "\n" ++ header ++ indices.map(line).mkString ++ "\n"

    val player: String =
      m.turn.player match {
        case Player.First =>
          s"${Console.GREEN}First player${Console.RESET}"
        case Player.Second =>
          s"${Console.YELLOW}Second player${Console.RESET}"
      }

    println(grid)

    if (m.onGoing) {
      print(s"This is now $player's turn.")

      @scala.annotation.tailrec
      def readPosition(): Msg = {
        print(
          s"""\n${player.capitalize}, please enter the position "row,column" of allowed move ($colorOfAllowedMoves) : """
        )
        val str = scala.io.StdIn.readLine()

        val posOpt: Option[Position] =
          scala.util
            .Try {
              val l = str.split(",").map(_.toInt)
              Position(l(0), l(1))
            }
            .toOption
            .filter(allowedMoves.contains(_))

        posOpt match {
          case Some(p) => Action.AMove(Move(p))
          case None =>
            println(
              s"""Sorry but this is not the position "row,column" of an allowed move ($colorOfAllowedMoves)."""
            )
            readPosition()
        }

      }

      readPosition()

    } else {
      // La partie est en cours
      val promptEndGame
          : String = s"Congratulations $player, you've just won the game!" ++
        s"\n\nPress any key to start a new game."
      print(promptEndGame)
      scala.io.StdIn.readLine()
      Action.NewGame
    }
  }
}
