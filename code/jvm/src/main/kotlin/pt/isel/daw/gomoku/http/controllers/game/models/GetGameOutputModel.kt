package pt.isel.daw.gomoku.http.controllers.game.models

data class GetGameOutputModel(
    val gameState: String,
    val opponentColor: String?
)
