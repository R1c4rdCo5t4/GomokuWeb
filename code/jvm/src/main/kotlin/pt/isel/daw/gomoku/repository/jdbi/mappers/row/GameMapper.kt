package pt.isel.daw.gomoku.repository.jdbi.mappers.row

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.domain.game.Color
import pt.isel.daw.gomoku.domain.game.Game
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.repository.jdbi.mappers.json.BoardMapper
import pt.isel.daw.gomoku.repository.jdbi.mappers.json.GameConfigMapper
import java.sql.ResultSet
import java.sql.SQLException

class GameMapper : RowMapper<Game> {

    @Throws(SQLException::class)
    override fun map(rs: ResultSet?, ctx: StatementContext?): Game {
        requireNotNull(rs) { "ResultSet cannot be null" }

        return Game(
            id = rs.getInt("id"),
            board = mapBoard(rs.getString("board")),
            blackPlayerId = rs.getInt("black_player"),
            whitePlayerId = rs.getInt("white_player"),
            config = mapGameConfig(rs.getString("config")),
            turn = mapTurn(rs.getString("turn")),
            state = mapGameState(rs.getString("state"))
        )
    }

    private fun mapBoard(boardJson: String): Board =
        BoardMapper.deserializeBoardFromJson(boardJson)

    private fun mapGameConfig(configJson: String): GameConfig =
        GameConfigMapper.deserializeGameConfigFromJson(configJson)

    private fun mapTurn(turnString: String): Color =
        when (turnString) {
            "B" -> Color.BLACK
            "W" -> Color.WHITE
            else -> throw IllegalArgumentException("Invalid Turn String: $turnString")
        }

    private fun mapGameState(stateString: String): GameState =
        when (stateString) {
            "R" -> GameState.RUNNING
            "B" -> GameState.BLACK_WON
            "W" -> GameState.WHITE_WON
            "D" -> GameState.DRAW
            else -> throw IllegalArgumentException("Invalid GameState String: $stateString")
        }
}
