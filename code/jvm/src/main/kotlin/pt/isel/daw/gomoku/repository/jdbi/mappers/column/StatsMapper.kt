package pt.isel.daw.gomoku.repository.jdbi.mappers.column

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.daw.gomoku.domain.user.Stats
import java.sql.ResultSet
import java.sql.SQLException

class StatsMapper : ColumnMapper<Stats> {

    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): Stats =
        Stats(
            r.getInt("rating"),
            r.getInt("games_played"),
            r.getInt("wins"),
            r.getInt("draws"),
            r.getInt("losses")
        )
}
