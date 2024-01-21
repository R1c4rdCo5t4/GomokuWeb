package pt.isel.daw.gomoku.repository

import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.domain.game.Game
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.services.models.MatchModel

interface GameRepository {
    fun createGame(board: Board, blackPlayerId: Int, whitePlayerId: Int, config: GameConfig): Int
    fun getGame(id: Int): Game
    fun getMatch(userId: Int): MatchModel
    fun updateGame(game: Game)
    fun isGame(id: Int): Boolean
    fun isGameOver(id: Int): Boolean
    fun isUserInGame(userId: Int): Boolean
    fun getGames(userId: Int?, state: GameState?, skip: Int, limit: Int, sort: String): List<Game>
    fun getTotalGames(userId: Int?, state: GameState?): Int
}
