package pt.isel.daw.gomoku.services

import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.domain.exceptions.LobbyException.LobbyIsFullException
import pt.isel.daw.gomoku.domain.exceptions.LobbyException.UserAlreadyInGameException
import pt.isel.daw.gomoku.domain.exceptions.LobbyException.UserAlreadyInLobbyException
import pt.isel.daw.gomoku.domain.exceptions.LobbyException.UserNotInLobbyException
import pt.isel.daw.gomoku.domain.exceptions.UserException.UserNotFoundException
import pt.isel.daw.gomoku.domain.exceptions.requireOrThrow
import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.lobby.LobbyDomain
import pt.isel.daw.gomoku.domain.lobby.LobbyDomain.Companion.LOBBY_MAX_CAPACITY
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.repository.transaction.TransactionManager
import pt.isel.daw.gomoku.services.models.MatchModel

@Component
class LobbyService(
    private val tm: TransactionManager,
    private val domain: LobbyDomain
) {

    /**
     * Joins a lobby
     * @param user The user that wants to join the lobby
     * @param config The game's config
     * @return The game's id if a match was found, null otherwise
     * @throws LobbyIsFullException if the lobby is full
     * @throws UserAlreadyInLobbyException if the user is already in the lobby
     * @throws UserAlreadyInGameException if the user is already in a game
     */
    fun joinLobby(user: User, config: GameConfig): Unit = tm.run {
        val lobby = it.lobbyRepository.getLobby()
        requireOrThrow<LobbyIsFullException>(lobby.size < LOBBY_MAX_CAPACITY) { "Lobby is full, please try again later" }
        requireOrThrow<UserAlreadyInLobbyException>(!it.lobbyRepository.isUserInLobby(user.id)) { "User is already in lobby" }
        requireOrThrow<UserAlreadyInGameException>(!it.gameRepository.isUserInGame(user.id)) { "User is already in a game" }
        val opponent = domain.findMatch(user, lobby, config)
        if (opponent != null) {
            it.lobbyRepository.removeUserFromLobby(opponent.id)
            it.gameRepository.createGame(
                board = Board(config.boardSize),
                blackPlayerId = opponent.id,
                whitePlayerId = user.id,
                config = config
            )
        } else {
            it.lobbyRepository.addUserToLobby(user.id, config)
        }
    }

    /**
     * Finds the match the user is in
     * @param userId The user's id
     * @return The game's id if the user is in a game, null otherwise
     * @throws UserNotFoundException if the user was not found
     */
    fun findMatch(userId: Int): MatchModel? = tm.run {
        requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not found" }
        if (it.gameRepository.isUserInGame(userId)) {
            it.gameRepository.getMatch(userId)
        } else {
            requireOrThrow<UserNotInLobbyException>(it.lobbyRepository.isUserInLobby(userId)) { "User is not in lobby" }
            null
        }
    }

    /**
     * Leaves the lobby
     * @param userId The user's id
     * @throws UserNotFoundException if the user was not found
     * @throws UserNotInLobbyException if the user is not in the lobby
     * @throws UserAlreadyInGameException if the user is already in a game
     */
    fun leaveLobby(userId: Int): Unit = tm.run {
        requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not found" }
        requireOrThrow<UserNotInLobbyException>(it.lobbyRepository.isUserInLobby(userId)) { "User is not in lobby" }
        requireOrThrow<UserAlreadyInGameException>(!it.gameRepository.isUserInGame(userId)) { "User is already in a game" }
        it.lobbyRepository.removeUserFromLobby(userId)
    }
}
