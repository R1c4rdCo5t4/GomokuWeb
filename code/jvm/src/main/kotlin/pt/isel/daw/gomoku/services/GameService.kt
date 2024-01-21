package pt.isel.daw.gomoku.services

import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.domain.exceptions.GameException.GameNotFoundException
import pt.isel.daw.gomoku.domain.exceptions.GameException.UserNotInGameException
import pt.isel.daw.gomoku.domain.exceptions.UserException.UserNotFoundException
import pt.isel.daw.gomoku.domain.exceptions.requireOrThrow
import pt.isel.daw.gomoku.domain.game.Color
import pt.isel.daw.gomoku.domain.game.Coordinate
import pt.isel.daw.gomoku.domain.game.Game
import pt.isel.daw.gomoku.domain.game.GameDomain
import pt.isel.daw.gomoku.domain.game.GameDomain.Companion.RATING_CHANGE
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.domain.game.Move
import pt.isel.daw.gomoku.domain.user.Stats
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.repository.transaction.TransactionManager
import pt.isel.daw.gomoku.services.models.GameModel
import pt.isel.daw.gomoku.services.models.GamesModel

@Component
class GameService(
    private val tm: TransactionManager,
    private val domain: GameDomain
) {
    /**
     * Gets a game by id
     * @param id The game's id
     * @return The game
     * @throws GameNotFoundException if the game was not found
     */
    fun getGame(id: Int): Game = tm.run {
        requireOrThrow<GameNotFoundException>(it.gameRepository.isGame(id)) { "Game was not found" }
        it.gameRepository.getGame(id)
    }

    /**
     * Allows a user to play a game
     * @param userId The user's id of the user that wants to make a move
     * @param row The row of the move
     * @param col The column of the move
     */
    fun playGame(userId: Int, row: Int, col: Int): Unit = tm.run {
        val game = getGameByUserId(userId)
        val color = if (userId == game.whitePlayerId) Color.WHITE else Color.BLACK
        domain.makeMove(game, Move(Coordinate(row, col), color)).also { newGame ->
            it.gameRepository.updateGame(newGame)
            if (newGame.state != GameState.RUNNING) updateUsersStats(newGame)
        }
    }

    /**
     * Gets the games that a user is/was in
     * @param username Username of the user to filter the games by (optional)
     * @param state The state of the games to filter by (optional)
     * @param skip The number of games to skip
     * @param limit The number of games to get
     * @param sort The sort order
     * @return The list of games
     * @throws UserNotFoundException if the user was not found
     */
    fun getGames(
        username: String?,
        state: GameState?,
        skip: Int,
        limit: Int,
        sort: String
    ): GamesModel = tm.run {
        val userId = username?.let { name ->
            requireOrThrow<UserNotFoundException>(it.userRepository.isUserByUsername(name)) {
                "User with name $name was not found"
            }
            it.userRepository.getUserByUsername(name).id
        }
        val games = it.gameRepository.getGames(userId, state, skip, limit, sort).map { game ->
            val black = it.userRepository.getUser(game.blackPlayerId)
            val white = it.userRepository.getUser(game.whitePlayerId)
            val opponent = username?.let { name -> if (name == black.name) Color.WHITE else Color.BLACK }
            GameModel(
                gameId = game.id,
                state = game.state,
                black = black,
                white = white,
                opponent = opponent
            )
        }
        val totalGames = it.gameRepository.getTotalGames(userId, state)
        GamesModel(games, totalGames)
    }

    /**
     * Leaves a game
     * @param userId The user's id
     */
    fun leaveGame(userId: Int): Unit = tm.run {
        val game = getGameByUserId(userId)
        val state = if (userId == game.whitePlayerId) GameState.BLACK_WON else GameState.WHITE_WON
        val newGame = game.copy(state = state)
        it.gameRepository.updateGame(newGame)
        updateUsersStats(newGame)
    }

    /**
     * Gets a game a user is playing
     * @param userId The user's id
     * @return The game
     * @throws UserNotFoundException if the user was not found
     * @throws UserNotInGameException if the user is not in a game
     */
    private fun getGameByUserId(userId: Int): Game = tm.run {
        requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not found" }
        requireOrThrow<UserNotInGameException>(it.gameRepository.isUserInGame(userId)) { "User is not in a game" }
        it.gameRepository.getMatch(userId).let { match ->
            it.gameRepository.getGame(match.gameId)
        }
    }

    /**
     * Updates the stats of the users that played a game that ended
     * @param game The game
     */
    private fun updateUsersStats(game: Game) = tm.run {
        val blackPlayer = it.userRepository.getUser(game.blackPlayerId)
        val whitePlayer = it.userRepository.getUser(game.whitePlayerId)
        getUpdatedUserStats(game, blackPlayer).let { newStats ->
            it.userRepository.updateStats(game.blackPlayerId, newStats)
        }
        getUpdatedUserStats(game, whitePlayer).let { newStats ->
            it.userRepository.updateStats(game.whitePlayerId, newStats)
        }
    }

    /**
     * Gets the updated stats of a user
     * Should only be called in the case when the game is over
     * @param game the game
     * @param user the user
     * @return the updated stats
     */
    private fun getUpdatedUserStats(game: Game, user: User): Stats {
        val isBlackWinner = game.state == GameState.BLACK_WON && game.blackPlayerId == user.id
        val isWhiteWinner = game.state == GameState.WHITE_WON && game.whitePlayerId == user.id
        val isDraw = game.state == GameState.DRAW
        return when {
            isDraw ->
                user.stats.copy(
                    gamesPlayed = user.stats.gamesPlayed + 1,
                    draws = user.stats.draws + 1
                )
            isBlackWinner || isWhiteWinner ->
                user.stats.copy(
                    rating = user.stats.rating + RATING_CHANGE,
                    gamesPlayed = user.stats.gamesPlayed + 1,
                    wins = user.stats.wins + 1
                )
            else ->
                user.stats.copy(
                    rating = if (user.stats.rating < RATING_CHANGE) 0 else user.stats.rating - RATING_CHANGE,
                    gamesPlayed = user.stats.gamesPlayed + 1,
                    losses = user.stats.losses + 1
                )
        }
    }
}
