package pt.isel.daw.gomoku.domain.game

/**
 * Represents a piece in the game
 * @property coord The coordinate of the piece
 * @property color The color of the piece
 */
data class Piece(val coord: Coordinate, val color: Color)
