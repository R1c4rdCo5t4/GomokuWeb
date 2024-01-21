package pt.isel.daw.gomoku.domain.lobby

import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.user.User
import kotlin.math.abs

/**
 * The domain of the lobby containing all the lobby logic
 */
@Component
class LobbyDomain {

    /**
     * Gets the user with the closest rating to the user in the lobby if it exists (matchmaking algorithm)
     * @param user - the user searching for a match
     * @param lobby - the lobby containing the users also searching for a match
     * @return - the user with the closes rating to the user
     */
    fun findMatch(user: User, lobby: Lobby, config: GameConfig): User? {
        return if (lobby.players.isEmpty()) {
            null
        } else {
            lobby.players.firstOrNull {
                abs(it.user.stats.rating - user.stats.rating) <= MAX_RATING_DIFF && it.config == config
            }?.user
        }
    }

    companion object {
        const val LOBBY_MAX_CAPACITY = 50
        const val MAX_RATING_DIFF = 32
    }
}
