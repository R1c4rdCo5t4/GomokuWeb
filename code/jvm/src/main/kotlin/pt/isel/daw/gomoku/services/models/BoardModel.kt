package pt.isel.daw.gomoku.services.models

import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.services.models.PieceModel.Companion.toModel

data class BoardModel(val size: Int, val pieces: List<PieceModel>) {

    companion object {
        fun Board.toModel() = BoardModel(size, pieces.map { it.toModel() })
    }
}
