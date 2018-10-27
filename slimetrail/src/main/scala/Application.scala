package slimetrail

/** Simplified Skeleton of an application */
trait Application { self =>

  /** A value of type {{{Model}}} represents the state of the application at some given point in time.
    * You can see the state of an application as the snapshot of the application memory (heap + stack)
    * at any moment. The sole knowledge of the state (the value of type {{{Model}}} for some instant t)
    * must enable knowing precisely all information of the application at that moment.
    *
    * The model must not include presentation information, only the data needed for the application at
    * that moment.
    */
  type Model
  val initialModel: Model

  /** Messages are signals making the application transition from one state (a value of {{{Model}}}) to another.
    * They representent all events the application has to react to. The application react by switching state.
    */
  type Msg

  /** Compute the new state of the application (the new value of type {{{Model}}}) in response to an event (a message  if type {{{Msg}}}).*/
  def update(message: Msg, model: Model): Model

  // Debugging
  @inline protected def debug: Boolean = false

  @inline protected final def log(s: => String): Unit =
    if (debug) println(s)
}
