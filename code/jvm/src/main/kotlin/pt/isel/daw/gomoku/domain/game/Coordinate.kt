package pt.isel.daw.gomoku.domain.game

/**
 * Represents a coordinate in the board
 * @property row the row of the coordinate
 * @property col the column of the coordinate
 */
data class Coordinate(val row: Int, val col: Int) {
    init {
        require(row > 0) { "Row must be positive" }
        require(col > 0) { "Column must be positive" }
    }
}
