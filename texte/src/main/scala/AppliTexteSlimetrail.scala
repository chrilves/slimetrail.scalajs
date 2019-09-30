package slimetrail.texte

import slimetrail._

/** Interface texte d'une application Slimetrail.*/
final class AppliTexteSlimetrail(taille: Int)
    extends AppSlimetrail(taille)
    with ApplicationTexteElm {

  /** Affiche l'état de la partie et gère les évènements afin de générer un message.*/
  def vue(m: Partie): Msg = {
    log(s"[Slimetrail] Calcul de la vue")

    val coupsValides: Set[Position] = m.coupsValides.keySet

    val couleurDeCoupsValide: String =
      m.tour.joueur match {
        case Joueur.Premier => s"${Console.GREEN}C${Console.RESET}"
        case Joueur.Second  => s"${Console.YELLOW}C${Console.RESET}"
      }

    val couleurCasesCoup: Map[Position, String] =
      coupsValides.map((_, couleurDeCoupsValide)).toMap ++
        Map(
          m.objectifPremierJoueur -> s"${Console.GREEN}P${Console.RESET}",
          m.objectifSecondJoueur -> s"${Console.YELLOW}S${Console.RESET}",
          m.positionActuelle -> s"${Console.RED}A${Console.RESET}"
        )

    val hexagons: Hexa[String] =
      m.grille
        .indexedMap {
          case (pos, Case(visitee)) =>
            if (visitee)
              "X"
            else
              couleurCasesCoup.getOrElse(pos, " ")
        }

    val indices: List[Int] =
      (0 to (m.taille - 1)).toList

    val hline: String = ("-" * (3 * (m.taille + 1) + 1)) ++ "\n"

    val entete: String =
      "   -" ++
        ("-" * (3 * m.taille)) ++
        "\n   |" ++
        indices.map { i =>
          s"$i".padTo(2, ' ') ++ "|"
        }.mkString ++
        "\n" ++
        hline

    def ligne(i: Int): String =
      "|" ++ s"$i".padTo(2, ' ') ++ "|" ++
        (for {
          j <- indices
          s <- hexagons.get(Position(i, j)).toList
        } yield s"$s |").mkString ++
        "\n" ++
        hline

    val grille: String =
      "\n" ++ entete ++ indices.map(ligne).mkString ++ "\n"

    val joueur: String =
      m.tour.joueur match {
        case Joueur.Premier =>
          s"${Console.GREEN}premier.e joueur.euse${Console.RESET}"
        case Joueur.Second =>
          s"${Console.YELLOW}second.e joueur.euse${Console.RESET}"
      }

    println(grille)

    if (m.enCours) {
      print(s"C'est au/à la $joueur de jouer.")

      @scala.annotation.tailrec
      def litPosition(): Msg = {
        print(
          s"""\n${joueur.capitalize}, entez la position "ligne,colonne" d'un coup valide ($couleurDeCoupsValide) : """
        )
        val str = scala.io.StdIn.readLine()

        val posOpt: Option[Position] =
          scala.util
            .Try {
              val l = str.split(",").map(_.toInt)
              Position(l(0), l(1))
            }
            .toOption
            .filter(coupsValides.contains(_))

        posOpt match {
          case Some(p) => UnCoup(Coup(p))
          case None =>
            println(
              s"""Ceci n'est pas la position "ligne,colonne" d'un coup valide ($couleurDeCoupsValide)."""
            )
            litPosition()
        }

      }

      litPosition()

    } else {
      // La partie est en cours
      val invitePartieFinie
          : String = s"Bravo $joueur, vous venez de remporter la partie!" ++
        s"\n\nEntrez n'importe quelle touche pour démarrer une nouvelle partie."
      print(invitePartieFinie)
      scala.io.StdIn.readLine()
      NouvellePartie

    }
  }
}
