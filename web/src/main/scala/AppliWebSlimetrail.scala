package slimetrail.web

import html.Html
import slimetrail._
import outils._

final class AppliWebSlimetrail(taille: Int)
    extends AppSlimetrail(taille)
    with ApplicationWebElm {

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
          m.objectifPremierJoueur -> "objectif-premier-joueur",
          m.objectifSecondJoueur -> "objectif-second-joueur",
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

            val centre = Partie.coordonnees(pos)

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
          (Partie.coordonnees(m.positionDeDepart),
           Joueur.Premier: Joueur,
           Nil: List[Html[Msg]])) {
          case ((p1, j, acc), coup) =>
            val laclasse: String =
              j match {
                case Joueur.Premier => "coup-premier-joueur"
                case Joueur.Second  => "coup-second-joueur"
              }

            val p2 = Partie.coordonnees(coup.position)

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
      viewBox(s"-1 ${-m.demiHauteur} ${m.largeur} ${2 * m.demiHauteur}"),
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
          polygon(style("stroke:black;"),
                  points(Point.hexagonRayon1Centre0.mkString(" ")))()
        ),
        g(id("hexagones"))(hexagons: _*),
        g(id("chemin"))(cheminDesCoups: _*)
      ),
      g(id("regles"))(
        text(
          x(s"${-0.5 / (m.taille - 2)}"),
          y(s"${-0.8 * m.demiHauteur}"),
          style(s"font-size: ${m.taille}%;"),
          `class`("titre")
        )(texte("Slimetrail")))
    )
  }
}
