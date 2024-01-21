package pt.isel.daw.gomoku.domain.game

/**
 * Represents a move in the game
 * @property coord the coordinate of the move
 * @property color the color of the move
 */
data class Move(val coord: Coordinate, val color: Color)
