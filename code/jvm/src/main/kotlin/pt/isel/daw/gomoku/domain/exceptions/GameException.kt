package pt.isel.daw.gomoku.domain.exceptions

/**
 * Represents a game exception
 * @param msg the exception message
 */
sealed class GameException(msg: String) : Exception(msg) {
    class GameNotFoundException(msg: String) : GameException(msg)
    class GameAlreadyOverException(msg: String) : GameException(msg)
    class NotPlayerTurnException(msg: String) : GameException(msg)
    class InvalidMoveException(msg: String) : GameException(msg)
    class UserNotInGameException(msg: String) : GameException(msg)
}
