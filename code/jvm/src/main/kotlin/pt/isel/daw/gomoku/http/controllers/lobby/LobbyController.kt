package pt.isel.daw.gomoku.http.controllers.lobby

import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.http.controllers.lobby.models.FindMatchOutputModel
import pt.isel.daw.gomoku.http.controllers.lobby.models.JoinLobbyInputModel
import pt.isel.daw.gomoku.http.media.siren.SirenEntity
import pt.isel.daw.gomoku.http.media.siren.SubEntity
import pt.isel.daw.gomoku.http.pipeline.authentication.Session
import pt.isel.daw.gomoku.http.utils.Actions
import pt.isel.daw.gomoku.http.utils.Links
import pt.isel.daw.gomoku.http.utils.MediaTypes.PROBLEM_MEDIA_TYPE
import pt.isel.daw.gomoku.http.utils.MediaTypes.SIREN_MEDIA_TYPE
import pt.isel.daw.gomoku.http.utils.Rels
import pt.isel.daw.gomoku.http.utils.Uris
import pt.isel.daw.gomoku.services.LobbyService

@RestController
@RequestMapping(Uris.PREFIX, produces = [SIREN_MEDIA_TYPE, PROBLEM_MEDIA_TYPE])
class LobbyController(val services: LobbyService) {

    /**
     * Handles the request to join a lobby
     * @param body the request body (JoinLobbyInputModel)
     * @param session the user session
     * @return the response with the game's id if a match was found, null otherwise
     */
    @PostMapping(Uris.Lobby.JOIN)
    fun joinLobby(
        @Valid @RequestBody
        body: JoinLobbyInputModel,
        session: Session
    ): ResponseEntity<*> {
        services.joinLobby(session.user, GameConfig(body.boardSize, body.variant, body.opening))
        return SirenEntity<Unit>(
            clazz = listOf(Rels.Lobby.JOIN),
            actions = listOf(Actions.Lobby.findMatch(), Actions.Lobby.leave())
        ).ok()
    }

    /**
     * Handles the request to get the match the user is in
     * @param session the user session
     * @return the response with the game's id if the user is in a game, null otherwise
     */
    @GetMapping(Uris.Lobby.FIND_MATCH)
    fun findMatch(session: Session): ResponseEntity<*> {
        val match = services.findMatch(session.user.id)
        return when (match) {
            null -> {
                val headers = HttpHeaders().apply {
                    set("Retry-After", RETRY_AFTER_SECONDS.toString())
                }
                SirenEntity<Unit>(
                    clazz = listOf(Rels.Lobby.FIND_MATCH),
                    actions = listOf(Actions.Lobby.findMatch(), Actions.Lobby.leave())
                ).accepted(headers)
            }
            else -> {
                SirenEntity(
                    clazz = listOf(Rels.Lobby.FIND_MATCH),
                    properties = FindMatchOutputModel(if (match.blackPlayerId == session.user.id) "B" else "W"),
                    actions = listOf(
                        Actions.Game.getById(match.gameId),
                        Actions.Game.getOpponent(match.getOpponent(session.user.id))
                    ),
                    entities = listOf(
                        SubEntity.EmbeddedLink(
                            rel = listOf(Rels.Game.GAME),
                            href = Uris.Game.game(match.gameId)
                        )
                    )
                ).ok()
            }
        }
    }

    /**
     * Handles the request to leave the lobby
     * @param session the user session
     * @return the response
     */
    @DeleteMapping(Uris.Lobby.LEAVE)
    fun leaveLobby(session: Session): ResponseEntity<*> {
        services.leaveLobby(session.user.id)
        return SirenEntity<Unit>(
            clazz = listOf(Rels.Lobby.LEAVE),
            links = listOf(
                Links.home(),
                Links.userHome()
            )
        ).ok()
    }

    companion object {
        const val RETRY_AFTER_SECONDS = 3
    }
}
