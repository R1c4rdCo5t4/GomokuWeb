package pt.isel.daw.gomoku.domain.game

import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.domain.exceptions.GameException.GameAlreadyOverException
import pt.isel.daw.gomoku.domain.exceptions.GameException.InvalidMoveException
import pt.isel.daw.gomoku.domain.exceptions.GameException.NotPlayerTurnException
import pt.isel.daw.gomoku.domain.exceptions.requireOrThrow
import pt.isel.daw.gomoku.domain.game.GameConfig.Companion.OPENING_MOVES

/**
 * The domain of the game containing all the game logic
 */
@Component
class GameDomain {

    /**
     * Makes a move in a game
     * @param game the game to make the move in
     * @param move the move to make
     * @return the game after the move was made
     * @throws GameAlreadyOverException if the game has already ended
     * @throws NotPlayerTurnException if it's not the player's turn to play
     * @throws InvalidMoveException if the move is invalid
     */
    fun makeMove(game: Game, move: Move): Game {
        requireOrThrow<GameAlreadyOverException>(game.state == GameState.RUNNING) { "The game has already ended" }
        requireOrThrow<NotPlayerTurnException>(move.color == game.turn) { "It's not the player's turn to play" }
        requireOrThrow<InvalidMoveException>(game.board[move.coord.row, move.coord.col] == null) { "The position ${move.coord} is already occupied" }
        validateMove(game.board, move, game.config)
        val newBoard = game.board.addPiece(move.coord, game.turn)
        val newState = updateGameState(newBoard, move)
        val newTurn = if (newState != GameState.RUNNING) game.turn else game.turn.other()
        return game.copy(
            board = newBoard,
            turn = newTurn,
            state = newState
        )
    }

    /**
     * Updates the state of the game
     * @param board the board of the game
     * @param move the move that was made
     */
    fun updateGameState(board: Board, move: Move): GameState =
        if (checkWin(board, move)) {
            if (move.color == Color.BLACK) GameState.BLACK_WON else GameState.WHITE_WON
        } else {
            if (board.isFull) GameState.DRAW else GameState.RUNNING
        }

    /**
     * Checks if a player has won the game with the given move
     * @param board the board of the game
     * @param move the move that was made
     * @return true if the player has won the game, false otherwise
     */
    fun checkWin(board: Board, move: Move): Boolean {
        fun checkChain(dr: Int, dc: Int): Boolean {
            fun checkDirection(dir: Int): Int {
                var depth = 0
                while (depth < NUM_IN_A_ROW) {
                    val row = move.coord.row + (dr * dir * depth)
                    val col = move.coord.col + (dc * dir * depth)
                    if (row !in board.boardRange || col !in board.boardRange) {
                        break
                    }
                    val piece = board[row, col]
                    if (piece != null && piece.color == move.color) {
                        depth++
                    } else {
                        break
                    }
                }
                return depth
            }
            val depthForward = checkDirection(1)
            val depthBackward = checkDirection(-1)
            return depthForward + depthBackward - 1 == NUM_IN_A_ROW
        }
        return allDirections.any { checkChain(it.first, it.second) }
    }

    /**
     * Validates a move according to the game rules
     * @param board the board of the game
     * @param move the move to be validated
     * @param config the game configuration containing the rules
     */
    fun validateMove(board: Board, move: Move, config: GameConfig): Boolean =
        if (board.pieces.size <= OPENING_MOVES) {
            config.opening.validateMove(board, move)
        } else {
            config.variant.validateMove(board, move)
        }

    companion object {
        const val NUM_IN_A_ROW = 5
        const val RATING_CHANGE = 8
        val allDirections = listOf(
            Pair(0, 1), // right
            Pair(0, -1), // left
            Pair(1, 0), // bottom
            Pair(-1, 0), // top
            Pair(1, 1), // down right
            Pair(-1, -1), // top left
            Pair(1, -1), // bottom left
            Pair(-1, 1) // top right
        )
    }
}
