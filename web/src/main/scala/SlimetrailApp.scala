package slimetrail.web

import html.Html
import slimetrail._
import outils._

object SlimetrailApp extends ApplicationElm {
  private val debug: Boolean = true

  @inline private def log(s: => String): Unit =
    if (debug) println(s)

  type Model = Partie
  type Msg = Coup

  val modelInitial: Partie = Partie.debut

  def miseAJour(msg: Coup, model: Partie): Partie =
    model.jouerUnCoup(msg, model).getOrElse(model)

  def vue(m: Partie): Html[Msg] = {
    import html.syntax._
    log(s"[Slimetrail] Calcul de la vue")

    val coupsValides: Set[Position] = m.coupsValides.keySet

    val classDeCoupsValide: String =
      m.tour.joueur match {
        case Joueur.Premier => "coup-valide-premier-joueur"
        case Joueur.Second  => "coup-valide-second-joueur"
      }

    val classesCasesCoup: Map[Position, String] =
      coupsValides.map((_, classDeCoupsValide)).toMap ++
        Map(
          Partie.objectifPremierJoueur -> "objectif-premier-joueur",
          Partie.objectifSecondJoueur -> "objectif-second-joueur",
          m.positionActuelle -> "position-actuelle"
        )

    val hexagons: List[Html[Msg]] =
      m.grille
        .indexedMap {
          case (pos, Case(visitee)) =>
            val laclasse =
              if (visitee)
                "case-visitee"
              else
                classesCasesCoup.getOrElse(pos, "case-vide")

            val reaction =
              if (coupsValides.contains(pos)) onclick(Coup(pos)) else nop

            val centre = (haut ** pos.haut.toDouble) + (bas ** pos.bas.toDouble)

            use(
              xlinkHref("#hexagon"),
              x(s"${centre.x}"),
              y(s"${centre.y}"),
              width("2"),
              height("2"),
              reaction,
              `class`(laclasse)
            )()
        }
        .toVector
        .toList

    svg(
      viewBox(s"-1 ${-demiHauteur} ${largeur} ${2 * demiHauteur}"),
      transform("scale(1, -1)"),
      style("stroke-width: 0.01;")
    )(
      (symbol(id("hexagon"),
              x("-1"),
              y("-1"),
              width("2"),
              height("2"),
              viewBox("-1 -1 2 2"))(
        polygon(style("stroke:black;"), points(hexagon.mkString(" ")))()
      ) ::
        hexagons): _*
    )
  }

  val taille: Int = 10

  val largeur: Double = 3 * taille.toDouble - 1
  val demiHauteur: Double = taille * math.sin(math.Pi * 60 / 180)

  val hexagon: List[Point] =
    List(
      Point.polarDeg(1, 0 * 60),
      Point.polarDeg(1, 1 * 60),
      Point.polarDeg(1, 2 * 60),
      Point.polarDeg(1, 3 * 60),
      Point.polarDeg(1, 4 * 60),
      Point.polarDeg(1, 5 * 60)
    )

  val haut: Point = Point.polarDeg(math.sqrt(3), 30)
  val bas: Point = Point.polarDeg(math.sqrt(3), -30)
}
