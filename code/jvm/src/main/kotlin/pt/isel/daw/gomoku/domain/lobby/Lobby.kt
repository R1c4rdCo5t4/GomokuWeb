package pt.isel.daw.gomoku.domain.lobby

/**
 * Represents a lobby containing all the players waiting to join a game
 * @property players the list of players in the lobby
 */
data class Lobby(val players: List<Player> = emptyList()) {
    val size get() = players.size
}
