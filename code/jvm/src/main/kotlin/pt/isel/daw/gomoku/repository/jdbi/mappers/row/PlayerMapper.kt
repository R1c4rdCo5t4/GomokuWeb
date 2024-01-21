package pt.isel.daw.gomoku.repository.jdbi.mappers.row

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.gomoku.domain.lobby.Player
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.repository.jdbi.mappers.column.StatsMapper
import pt.isel.daw.gomoku.repository.jdbi.mappers.json.GameConfigMapper
import java.sql.ResultSet
import java.sql.SQLException

class PlayerMapper : RowMapper<Player> {

    @Throws(SQLException::class)
    override fun map(rs: ResultSet?, ctx: StatementContext?): Player {
        requireNotNull(rs) { "ResultSet cannot be null" }
        return Player(
            user = User(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                passwordHash = rs.getString("password_hash"),
                stats = StatsMapper().map(rs, 5, ctx)
            ),
            config = GameConfigMapper.deserializeGameConfigFromJson(rs.getString("config"))
        )
    }
}
