package pt.isel.daw.gomoku.services.models

import pt.isel.daw.gomoku.domain.game.Piece

data class PieceModel(val row: Int, val col: Int, val color: String) {

    companion object {
        fun Piece.toModel() = PieceModel(coord.row, coord.col, color.toString())
    }
}
