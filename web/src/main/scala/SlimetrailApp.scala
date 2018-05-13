package slimetrail.web

import html.Html
import slimetrail._
import outils._

object SlimetrailApp extends ApplicationElm {
  private val debug: Boolean = false

  @inline private def log(s: => String): Unit =
    if (debug) println(s)

  type Model = Partie

  sealed abstract class Msg
  final case class UnCoup(coup: Coup) extends Msg
  final case object NouvellePartie extends Msg

  val modelInitial: Partie = Partie.debut

  def miseAJour(msg: Msg, model: Partie): Partie =
    msg match {
      case UnCoup(c)      => model.jouerUnCoup(c, model).getOrElse(model)
      case NouvellePartie => Partie.debut
    }

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

            val reaction: Parametre[Msg] =
              if (coupsValides.contains(pos) && m.enCours)
                onclick(UnCoup(Coup(pos)))
              else
                nop

            val centre = coordonnees(pos)

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

    val cheminDesCoups: List[Html[Msg]] = {
      val acc =
        m.historique.foldLeft(
          (coordonnees(Partie.debut.positionActuelle),
           Joueur.Premier: Joueur,
           Nil: List[Html[Msg]])) {
          case ((p1, j, acc), coup) =>
            val laclasse: String =
              j match {
                case Joueur.Premier => "coup-premier-joueur"
                case Joueur.Second  => "coup-second-joueur"
              }

            val p2 = coordonnees(coup.position)

            val nouvelleLigne =
              line(
                x1(s"${p1.x}"),
                y1(s"${p1.y}"),
                x2(s"${p2.x}"),
                y2(s"${p2.y}"),
                `class`(laclasse),
                style("stroke-width:0.1;")
              )()

            (p2, j.suivant, nouvelleLigne :: acc)
        }
      acc._3
    }

    svg(
      viewBox(s"-1 ${-demiHauteur} ${largeur} ${2 * demiHauteur}"),
      style("stroke-width: 0.01;"),
      if (!m.enCours) onclick(NouvellePartie: Msg) else nop
    )(
      g(id("plateau"), transform("scale(1,-1)"))(
        symbol(id("hexagon"),
               x("-1"),
               y("-1"),
               width("2"),
               height("2"),
               viewBox("-1 -1 2 2"))(
          polygon(style("stroke:black;"), points(hexagon.mkString(" ")))()
        ),
        g(id("hexagones"))(hexagons: _*),
        g(id("chemin"))(cheminDesCoups: _*)
      ),
      g(id("regles"))(
        text(
          x("0"),
          y(s"${-0.8 * demiHauteur}"),
          style("font-size: 10%;"),
          `class`("titre")
        )(texte("Slimetrail")))
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

  def coordonnees(pos: Position): Point =
    (haut ** pos.haut.toDouble) + (bas ** pos.bas.toDouble)
}
