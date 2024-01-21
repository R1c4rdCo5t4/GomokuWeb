package pt.isel.daw.gomoku.services.models

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * Represents the match found by the lobby between two users
 * @param gameId The game's id
 * @param blackPlayerId The black player's id
 * @param whitePlayerId The white player's id
 */
data class MatchModel(
    @ColumnName("id") val gameId: Int,
    @ColumnName("black_player") val blackPlayerId: Int,
    @ColumnName("white_player") val whitePlayerId: Int
) {
    fun getOpponent(userId: Int): Int = if (userId == blackPlayerId) whitePlayerId else blackPlayerId
}
