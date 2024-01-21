package pt.isel.daw.gomoku.repository.jdbi.mappers.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jdbi.v3.core.statement.Update
import org.postgresql.util.PGobject
import pt.isel.daw.gomoku.domain.game.Board

class BoardMapper {

    companion object {
        fun Update.bindBoard(name: String, board: Board): Update = run {
            bind(
                name,
                PGobject().apply {
                    type = "jsonb"
                    value = serializeBoardToJson(board)
                }
            )
        }

        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        private fun serializeBoardToJson(board: Board): String = objectMapper.writeValueAsString(board)

        fun deserializeBoardFromJson(json: String): Board = objectMapper.readValue(json, Board::class.java)
    }
}
