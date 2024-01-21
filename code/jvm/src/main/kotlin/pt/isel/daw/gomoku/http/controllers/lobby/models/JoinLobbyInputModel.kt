package pt.isel.daw.gomoku.http.controllers.lobby.models

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import pt.isel.daw.gomoku.http.utils.Regex

data class JoinLobbyInputModel(

    @field:Positive
    val boardSize: Int,

    @field:Pattern(regexp = Regex.VALID_STRING, message = Regex.VALID_STRING_MSG)
    val variant: String = "freestyle",

    @field:Pattern(regexp = Regex.VALID_STRING, message = Regex.VALID_STRING_MSG)
    val opening: String = "freestyle"
)
