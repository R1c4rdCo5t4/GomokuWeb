package pt.isel.daw.gomoku.repository

import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.lobby.Lobby

interface LobbyRepository {
    fun addUserToLobby(userId: Int, config: GameConfig)
    fun getLobby(): Lobby
    fun removeUserFromLobby(userId: Int)
    fun isUserInLobby(userId: Int): Boolean
}
