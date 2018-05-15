package slimetrail

/** Skelette simplifié d'une application de type Elm */
trait ApplicationElm { self =>

  /** Le modèle représente la donnée de l'application à chaque instant du temps.
    * La seule connaissance de la valeur du modèle doit permettre de savoir précisément
    * dans quel état se trouve l'application.
    *
    * Le modèle ne doit pas contenir d'élement de présentation, uniquement les informations
    * que l'application à chaque instant du temps.
    */
  type Model
  val modelInitial: Model

  /** Les messages sont les signaux qui permettent à l'application de changer d'état.
    * Ils répresentent tous les évènements, au sens du domaine métier, c'est a dire
    * du modèle, pour lesquels l'application doit réagit en mettant à jour sont modèle.
    */
  type Msg

  /** Calcule le nouvel état de l'application (la nouvelle valeur de Model),
    * en réponse à un évènement {{msg}}.
    */
  def miseAJour(message: Msg, model: Model): Model

  // Debugging "pragmatique"
  @inline protected def debug: Boolean = false

  @inline protected final def log(s: => String): Unit =
    if (debug) println(s)
}
