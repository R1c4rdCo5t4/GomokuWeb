package pt.isel.daw.gomoku.domain.game

import pt.isel.daw.gomoku.domain.exceptions.LobbyException.RuleNotImplementedException

/**
 * Represents a game variant
 * @property isImplemented whether the variant is implemented or not
 */
enum class Variant(override val isImplemented: Boolean = false) : Rule {
    FREESTYLE(true), PENTE, RENJU, SWAP_FIRST_MOVE, CARO, OMOK;

    override fun validateMove(board: Board, move: Move): Boolean {
        return when (this) {
            FREESTYLE -> true
            else -> throw RuleNotImplementedException("Variant '$this' is not implemented")
        }
    }
}
