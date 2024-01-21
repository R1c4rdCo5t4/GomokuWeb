package pt.isel.daw.gomoku.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.lobby.Lobby
import pt.isel.daw.gomoku.domain.lobby.Player
import pt.isel.daw.gomoku.repository.LobbyRepository
import pt.isel.daw.gomoku.repository.jdbi.mappers.json.GameConfigMapper.Companion.bindGameConfig

class JdbiLobbyRepository(
    private val handle: Handle
) : LobbyRepository {

    /**
     * Adds a user to the lobby
     * @param userId The user's id
     * @param config The game's config
     */
    override fun addUserToLobby(userId: Int, config: GameConfig) {
        handle.createUpdate(
            """
            insert into dbo.Lobby(user_id, config) 
            values (:userId, :config)
            """.trimIndent()
        )
            .bind("userId", userId)
            .bindGameConfig("config", config)
            .execute()
    }

    /**
     * Gets the lobby containing all the waiting users
     * @return The lobby
     */
    override fun getLobby() = Lobby(
        handle.createQuery(
            """
                select * from dbo.Lobby
                join dbo.User on dbo.Lobby.user_id = dbo.User.id
            """.trimIndent()
        )
            .mapTo<Player>()
            .list()
    )

    /**
     * Removes a user from the lobby
     * @param userId The user's id
     */
    override fun removeUserFromLobby(userId: Int) {
        handle.createUpdate("delete from dbo.Lobby where user_id = :userId")
            .bind("userId", userId)
            .execute()
    }

    /**
     * Checks if a user is in the lobby
     * @param userId The user's id
     * @return if the user is in the lobby
     */
    override fun isUserInLobby(userId: Int): Boolean =
        handle.createQuery("select count(*) from dbo.Lobby where user_id = :userId")
            .bind("userId", userId)
            .mapTo<Int>()
            .one() == 1
}
