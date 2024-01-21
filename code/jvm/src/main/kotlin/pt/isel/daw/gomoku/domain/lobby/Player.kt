package pt.isel.daw.gomoku.domain.lobby

import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.user.User

/**
 * Represents a player in the lobby
 * @property user The user in the lobby
 * @property config The game configuration
 */
data class Player(val user: User, val config: GameConfig)
