package pt.isel.daw.gomoku.services.models

import pt.isel.daw.gomoku.domain.user.Stats
import pt.isel.daw.gomoku.domain.user.User

data class UserModel(
    val id: Int,
    val name: String,
    val email: String,
    val stats: Stats
) {
    companion object {
        fun User.toModel() = UserModel(id, name, email, stats)
    }
}
