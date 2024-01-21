package pt.isel.daw.gomoku.domain.game

import pt.isel.daw.gomoku.domain.exceptions.LobbyException.RuleNotImplementedException

interface Rule {
    val isImplemented: Boolean

    fun validateMove(board: Board, move: Move): Boolean

    companion object {

        inline fun <reified T> to(rule: String): T where T : Enum<T>, T : Rule {
            val enumValue = enumValues<T>().find { it.name == rule.uppercase() }
            return when {
                enumValue != null && enumValue.isImplemented -> enumValue
                enumValue != null -> throw RuleNotImplementedException("${T::class.simpleName} '$rule' is not implemented")
                else -> throw IllegalArgumentException("${T::class.simpleName} '$rule' is invalid")
            }
        }
    }
}
