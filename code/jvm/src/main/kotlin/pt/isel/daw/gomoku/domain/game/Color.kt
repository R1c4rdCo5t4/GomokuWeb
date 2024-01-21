package pt.isel.daw.gomoku.domain.game

typealias Turn = Color

/**
 * Represents the color of a player or piece, as well as the turn of a player
 * @property BLACK the color black
 * @property WHITE the color white
 */
enum class Color {
    BLACK, WHITE;

    fun other() = if (this == BLACK) WHITE else BLACK

    override fun toString() = when (this) {
        BLACK -> "B"
        WHITE -> "W"
    }
}
