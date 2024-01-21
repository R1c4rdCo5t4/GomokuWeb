package pt.isel.daw.gomoku.http.controllers.game.models

import pt.isel.daw.gomoku.services.models.BoardModel

data class GetGameStateOutputModel(
    val board: BoardModel,
    val turn: String,
    val state: String
)
