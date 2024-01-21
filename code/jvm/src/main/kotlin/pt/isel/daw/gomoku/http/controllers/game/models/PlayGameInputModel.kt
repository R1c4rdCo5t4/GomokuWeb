package pt.isel.daw.gomoku.http.controllers.game.models

import jakarta.validation.constraints.Positive

data class PlayGameInputModel(
    @field:Positive
    val row: Int,

    @field:Positive
    val col: Int
)
