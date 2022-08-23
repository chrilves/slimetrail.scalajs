package slimetrail.web

import html.Html
import slimetrail._
import toolbox._

final class SlimetrailWebApp(size: Int) extends SlimetrailApp(size) with WebApplication:

  def view(m: GameState): Html[Msg] =
    import html.syntax._
    log(s"[Slimetrail] Computing view")

    val theStyle =
      styleTag(`type`("text/css"), media("screen"))(
        text("""
          |  .first-player-goal { fill:#0d0; }
          |  .first-player-allowed-move { fill:#afa; }
          |  .first-player-move { stroke: #0f0; }
          |
          |  .second-player-goal { fill:#dd0; }
          |  .second-player-allowed-move { fill:#ffa; }
          |  .second-player-move { stroke:#ff0; }
          |
          |  .empty-cell { fill: #ccf; }
          |  .visited-cell { fill: #000; }
          |
          | .current-position { fill: #a00;}
          |
          | .title { stroke: none; fill: white; }
        """.stripMargin)
      )

    val allowedMoves: Set[Position] = m.allowedMoves.keySet

    val classOfAllowedMoves: String =
      m.turn.player match {
        case Player.First  => "first-player-allowed-move"
        case Player.Second => "second-player-allowed-move"
      }

    val classOfCells: Map[Position, String] =
      allowedMoves.map((_, classOfAllowedMoves)).toMap ++
        Map(
          m.firstPlayerGoal  -> "first-player-goal",
          m.secondPlayerGoal -> "second-player-goal",
          m.currentPosition  -> "current-position"
        )

    val hexagons: List[Html[Msg]] =
      m.grid
        .indexedMap { case (pos, Cell(visited)) =>
          val theclass =
            if (visited)
              "visited-cell"
            else
              classOfCells.getOrElse(pos, "empty-cell")

          val reaction: Parameter[Msg] =
            if (allowedMoves.contains(pos) && m.onGoing)
              onclick(Action.AMove(Move(pos)))
            else
              nop

          val center = GameState.coordinates(pos)

          use(
            xlinkHref("#hexagon"),
            x(s"${center.x}"),
            y(s"${center.y}"),
            width("2"),
            height("2"),
            reaction,
            `class`(theclass)
          )()
        }
        .toVector
        .toList

    val cheminDesCoups: List[Html[Msg]] =
      val acc =
        m.history.foldLeft(
          (
            GameState.coordinates(m.startingPosition),
            Player.First: Player,
            Nil: List[Html[Msg]]
          )
        ) { case ((p1, j, acc), coup) =>
          val laclasse: String =
            j match {
              case Player.First  => "first-player-move"
              case Player.Second => "second-player-move"
            }

          val p2 = GameState.coordinates(coup.position)

          val nouvelleLigne =
            line(
              x1(s"${p1.x}"),
              y1(s"${p1.y}"),
              x2(s"${p2.x}"),
              y2(s"${p2.y}"),
              `class`(laclasse),
              style("stroke-width:0.1;")
            )()

          (p2, j.next, nouvelleLigne :: acc)
        }
      acc._3

    svg(
      viewBox(s"-1 ${-m.halfHeigh} ${m.width} ${2 * m.halfHeigh}"),
      style("""
          | stroke-width: 0.01;
          | background-color: rgb(103, 103, 238);
          | display:block;
          | position:fixed;
          | top:5%; left:5%; width:90%; height:90%;
          | margin:auto;
        """.stripMargin),
      if (!m.onGoing) onclick(Action.NewGame: Msg) else nop
    )(
      theStyle,
      g(id("board"), transform("scale(1,-1)"))(
        symbol(
          id("hexagon"),
          x("-1"),
          y("-1"),
          width("2"),
          height("2"),
          viewBox("-1 -1 2 2")
        )(
          polygon(
            style("stroke:black;"),
            points(Point.hexagonRadius1AtOrigin.mkString(" "))
          )()
        ),
        g(id("hexagons"))(hexagons: _*),
        g(id("path"))(cheminDesCoups: _*)
      ),
      g(id("rules"))(
        svgText(
          x(s"${-0.5 / (m.size - 2)}"),
          y(s"${-0.8 * m.halfHeigh}"),
          style(s"font-size: ${m.size}%;"),
          `class`("title")
        )(text("Slimetrail"))
      )
    )
