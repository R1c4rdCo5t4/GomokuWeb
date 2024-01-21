package pt.isel.daw.gomoku.services.models

import pt.isel.daw.gomoku.domain.game.Color
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.domain.user.User

data class GameModel(
    val gameId: Int,
    val state: GameState,
    val black: User,
    val white: User,
    val opponent: Color?
)
