package pt.isel.daw.gomoku.repository.jdbi.mappers.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jdbi.v3.core.statement.Update
import org.postgresql.util.PGobject
import pt.isel.daw.gomoku.domain.game.GameConfig

class GameConfigMapper {

    companion object {
        fun Update.bindGameConfig(name: String, config: GameConfig): Update = run {
            bind(
                name,
                PGobject().apply {
                    type = "jsonb"
                    value = serializeGameConfigToJson(config)
                }
            )
        }

        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        private fun serializeGameConfigToJson(config: GameConfig): String = objectMapper.writeValueAsString(config)

        fun deserializeGameConfigFromJson(json: String): GameConfig = objectMapper.readValue(json, GameConfig::class.java)
    }
}
