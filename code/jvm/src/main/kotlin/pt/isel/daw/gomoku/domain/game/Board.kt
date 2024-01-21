package pt.isel.daw.gomoku.domain.game

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Represents a board of a game
 * @property size The size of the board
 * @property pieces The pieces of the board
 */
data class Board(val size: Int, val pieces: List<Piece> = emptyList()) {

    @get:JsonIgnore
    val isFull get() = pieces.size == size * size

    @get:JsonIgnore
    val boardRange get() = 1..size

    @get:JsonIgnore
    val center get() = Coordinate(size / 2 + 1, size / 2 + 1)

    init {
        require(size > 0) { "Invalid board size" }
        require(pieces.all { it.coord.row in boardRange && it.coord.col in boardRange }) { "All pieces must be inside the board" }
        require(pieces.size == pieces.distinctBy { it.coord }.size) { "All pieces should have different coordinates" }
    }

    /**
     * Gets the piece in the given coordinate
     * @param row The row of the coordinate
     * @param col The column of the coordinate
     * @return The piece in the given coordinate or null if there is no piece in the given coordinate
     * @throws IllegalArgumentException if the given coordinate is outside the board
     */
    operator fun get(row: Int, col: Int): Piece? {
        require(row in boardRange && col in boardRange) { "The given coordinate is outside the board" }
        return pieces.find { it.coord.row == row && it.coord.col == col }
    }

    /**
     * Adds a piece to the board
     * @param coord The coordinate of the piece
     * @param color The color of the piece to add
     * @return The board with the added piece
     */
    fun addPiece(coord: Coordinate, color: Color): Board {
        require(!pieces.any { it.coord == coord }) { "There is already a piece in the given coordinate" }
        return Board(size, pieces + Piece(coord, color))
    }
}
