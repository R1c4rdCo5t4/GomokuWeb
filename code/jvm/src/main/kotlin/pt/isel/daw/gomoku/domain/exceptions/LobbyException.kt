package pt.isel.daw.gomoku.domain.exceptions

/**
 * Represents a lobby exception
 * @param msg the exception message
 */
sealed class LobbyException(msg: String) : Exception(msg) {
    class UserAlreadyInLobbyException(msg: String) : LobbyException(msg)
    class UserNotInLobbyException(msg: String) : LobbyException(msg)
    class LobbyIsFullException(msg: String) : LobbyException(msg)
    class UserAlreadyInGameException(msg: String) : LobbyException(msg)
    class RuleNotImplementedException(msg: String) : LobbyException(msg)
}
