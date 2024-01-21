package pt.isel.daw.gomoku.domain.game

/**
 * Represents the state of the game
 * @property RUNNING the game is running
 * @property WHITE_WON the white player won the game
 * @property BLACK_WON the black player won the game
 * @property DRAW the game ended in a draw
 */
enum class GameState {
    RUNNING, WHITE_WON, BLACK_WON, DRAW;

    override fun toString() = when (this) {
        RUNNING -> "R"
        WHITE_WON -> "W"
        BLACK_WON -> "B"
        DRAW -> "D"
    }

    companion object {
        fun String.toGameState() = when (this) {
            "R" -> RUNNING
            "W" -> WHITE_WON
            "B" -> BLACK_WON
            "D" -> DRAW
            else -> throw IllegalArgumentException("Invalid game state")
        }
    }
}
