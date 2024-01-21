package pt.isel.daw.gomoku.domain.game

import pt.isel.daw.gomoku.domain.exceptions.GameException.InvalidMoveException
import pt.isel.daw.gomoku.domain.exceptions.LobbyException.RuleNotImplementedException
import pt.isel.daw.gomoku.domain.exceptions.requireOrThrow

/**
 * Represents an opening rule in the game
 * @property isImplemented whether the opening is implemented or not
 */
enum class Opening(override val isImplemented: Boolean = false) : Rule {
    FREESTYLE(true), PRO(true), LONG_PRO(true), SWAP, SWAP2;

    override fun validateMove(board: Board, move: Move): Boolean {
        return when (this) {
            FREESTYLE -> true
            PRO -> validateProOpeningMove(board, move, PRO_OFFSET)
            LONG_PRO -> validateProOpeningMove(board, move, LONG_PRO_OFFSET)
            else -> throw RuleNotImplementedException("Opening '$this' is not implemented")
        }
    }

    private fun validateProOpeningMove(board: Board, move: Move, offset: Int): Boolean {
        if (board.pieces.isEmpty()) { // first black player move
            requireOrThrow<InvalidMoveException>(move.coord == board.center) {
                "First move must be in the center of the board"
            }
        } else if (board.pieces.size == 2) { // second black player move
            requireOrThrow<InvalidMoveException>(
                checkCoordinateIntersections(move.coord, board.center, offset)
            ) {
                "Second move must be at least $offset intersections away from the center"
            }
        }
        return true
    }

    private fun checkCoordinateIntersections(coord: Coordinate, center: Coordinate, offset: Int): Boolean {
        val rowRange = (center.row - offset)..(center.row + offset)
        val colRange = (center.col - offset)..(center.col + offset)
        return coord.col in colRange && coord.row in rowRange
    }

    companion object {
        const val PRO_OFFSET = 3
        const val LONG_PRO_OFFSET = 4
    }
}
