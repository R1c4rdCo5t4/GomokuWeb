package pt.isel.daw.gomoku.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.domain.game.Game
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.repository.GameRepository
import pt.isel.daw.gomoku.repository.jdbi.mappers.json.BoardMapper.Companion.bindBoard
import pt.isel.daw.gomoku.repository.jdbi.mappers.json.GameConfigMapper.Companion.bindGameConfig
import pt.isel.daw.gomoku.services.models.MatchModel

class JdbiGameRepository(
    private val handle: Handle
) : GameRepository {

    /**
     * Creates a new game
     * @param board The game's board
     * @param blackPlayerId The black player's id
     * @param whitePlayerId The white player's id
     * @param config The game's config
     * @return The game's id
     */
    override fun createGame(
        board: Board,
        blackPlayerId: Int,
        whitePlayerId: Int,
        config: GameConfig
    ): Int =
        handle.createUpdate(
            """
                insert into dbo.Game(board, black_player, white_player, config)
                values (:board, :blackPlayerId, :whitePlayerId, :config)
            """.trimIndent()
        )
            .bindBoard("board", board)
            .bind("blackPlayerId", blackPlayerId)
            .bind("whitePlayerId", whitePlayerId)
            .bindGameConfig("config", config)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()

    /**
     * Gets a game by id
     * @param id The game's id
     * @return The game
     */
    override fun getGame(id: Int): Game =
        handle.createQuery("select * from dbo.Game where id = :id")
            .bind("id", id)
            .mapTo<Game>()
            .single()

    /**
     * Gets the match found between two players containing the gameId, blackPlayerId and whitePlayerId
     * @param userId The user's id
     * @return The match
     */
    override fun getMatch(userId: Int): MatchModel =
        handle.createQuery(
            """
                select id, black_player, white_player 
                from dbo.Game where (black_player = :id or white_player = :id) and state = 'R'
            """.trimIndent()
        )
            .bind("id", userId)
            .mapTo<MatchModel>()
            .one()

    /**
     * Updates a game
     * @param game The game to update
     */
    override fun updateGame(game: Game) {
        handle.createUpdate("update dbo.Game set board = :board, turn = :turn, state = :state where id = :id")
            .bind("id", game.id)
            .bindBoard("board", game.board)
            .bind("turn", game.turn)
            .bind("state", game.state)
            .execute()
    }

    /**
     * Checks if a game exists
     * @param id The game's id
     * @return if the game exists
     */
    override fun isGame(id: Int): Boolean =
        handle.createQuery("select count(*) from dbo.Game where id = :id")
            .bind("id", id)
            .mapTo<Int>()
            .one() == 1

    /**
     * Checks if a game is over
     * @param id The game's id
     * @return if the game is over
     */
    override fun isGameOver(id: Int): Boolean =
        handle.createQuery("select count(*) from dbo.Game where id = :id and state != 'R'")
            .bind("id", id)
            .mapTo<Int>()
            .one() == 1

    /**
     * Checks if a user is in a game
     * @param userId The user's id
     * @return if the user is in a game
     */
    override fun isUserInGame(userId: Int): Boolean =
        handle.createQuery(
            """
                select count(*) from dbo.Game where (black_player = :id or white_player = :id) and state = 'R'
            """.trimIndent()
        )
            .bind("id", userId)
            .mapTo<Int>()
            .one() == 1

    /**
     * Gets a list of games
     * @param userId the user's id
     * @param skip the number of games to skip
     * @param limit the max number of games to include
     * @param sort the sort order
     * @return the list of games
     */
    override fun getGames(userId: Int?, state: GameState?, skip: Int, limit: Int, sort: String): List<Game> =
        handle.createQuery(
            """
                select * from dbo.Game
                where 
                    (:userId is null or black_player = :userId or white_player = :userId) and 
                    (:state is null or state = :state)
                order by id $sort
                offset :skip rows
                limit :limit
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("state", state?.toString())
            .bind("skip", skip)
            .bind("limit", limit)
            .mapTo<Game>()
            .list()

    /**
     * Gets the total count of the games with the given filters
     * @param userId the user's id
     * @param state the game's state
     * @return total count of games
     */
    override fun getTotalGames(userId: Int?, state: GameState?): Int =
        handle.createQuery(
            """
                select count(*)
                from dbo.Game
                where 
                    (:userId is null or black_player = :userId or white_player = :userId) and 
                    (:state is null or state = :state)
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("state", state?.toString())
            .mapTo<Int>()
            .one()
}
