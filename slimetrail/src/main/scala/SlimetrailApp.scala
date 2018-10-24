package slimetrail

/** Skeleton of Slimetrail applicaton. There is no UI here but
  * the {{Model}} is defined as a game state and events are
  * allowed moves for a game or starting a new game.
  */
abstract class SlimetrailApp(size: Int) extends Application {

  /** A Slimetrail application, at any given time, only needs to know the game state. */
  final type Model = GameState

  /** An application moves forward either with a player's move or
    * by setting a new game.
    */
  final type Msg = Action

  /** The intial state of a game, thus also of the application. */
  final lazy val initialModel: GameState = GameState.initial(size)

  /** Progress of a game (thus of the application) in response to
    * an event: a move or a new game request.
    */
  final def update(msg: Msg, model: GameState): GameState =
    msg match {
      case Action.AMove(c) => model.playAMove(c).getOrElse(model)
      case Action.NewGame  => GameState.initial(size)
    }
}

/** An action in the application */
sealed abstract class Action
object Action {

  /** The player plays a move*/
  final case class AMove(move: Move) extends Action

  /** The player request a new game*/
  final case object NewGame extends Action
}
