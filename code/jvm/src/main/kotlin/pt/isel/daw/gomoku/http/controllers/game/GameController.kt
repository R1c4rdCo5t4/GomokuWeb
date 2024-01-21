package pt.isel.daw.gomoku.http.controllers.game

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.gomoku.domain.game.GameState.Companion.toGameState
import pt.isel.daw.gomoku.http.controllers.game.models.GetGameOutputModel
import pt.isel.daw.gomoku.http.controllers.game.models.GetGameStateOutputModel
import pt.isel.daw.gomoku.http.controllers.game.models.GetGamesCountOutputModel
import pt.isel.daw.gomoku.http.controllers.game.models.PlayGameInputModel
import pt.isel.daw.gomoku.http.controllers.user.models.GetUserOutputModel
import pt.isel.daw.gomoku.http.media.siren.SirenEntity
import pt.isel.daw.gomoku.http.media.siren.SubEntity
import pt.isel.daw.gomoku.http.pipeline.authentication.Session
import pt.isel.daw.gomoku.http.utils.Actions
import pt.isel.daw.gomoku.http.utils.Links
import pt.isel.daw.gomoku.http.utils.MediaTypes.PROBLEM_MEDIA_TYPE
import pt.isel.daw.gomoku.http.utils.MediaTypes.SIREN_MEDIA_TYPE
import pt.isel.daw.gomoku.http.utils.Params
import pt.isel.daw.gomoku.http.utils.Rels
import pt.isel.daw.gomoku.http.utils.Uris
import pt.isel.daw.gomoku.services.GameService
import pt.isel.daw.gomoku.services.models.BoardModel.Companion.toModel

@RestController
@RequestMapping(Uris.PREFIX, produces = [SIREN_MEDIA_TYPE, PROBLEM_MEDIA_TYPE])
class GameController(private val services: GameService) {

    /**
     * Handles the request to get a game by id
     * @param gameId the game's id
     * @return the response with the game
     */
    @GetMapping(Uris.Game.GAME)
    fun getGame(@PathVariable gameId: Int): ResponseEntity<*> {
        val game = services.getGame(gameId)
        return SirenEntity(
            clazz = listOf(Rels.Game.GAME),
            properties = GetGameStateOutputModel(
                board = game.board.toModel(),
                turn = game.turn.toString(),
                state = game.state.toString()
            ),
            links = listOf(Links.self(Uris.Game.game(gameId).toString())),
            actions = listOf(
                Actions.Game.play(),
                Actions.Game.leave(),
                Actions.Game.getBlackPlayer(game.blackPlayerId),
                Actions.Game.getWhitePlayer(game.whitePlayerId)
            )
        ).ok()
    }

    /**
     * Handles the request to play a game
     * @param body the request body (PlayGameInputModel)
     * @param session the user session
     * @return the response
     */
    @PutMapping(Uris.Game.PLAY)
    fun playGame(@RequestBody body: PlayGameInputModel, session: Session): ResponseEntity<*> {
        services.playGame(session.user.id, body.row, body.col)
        return SirenEntity<Unit>(
            clazz = listOf(Rels.Game.PLAY),
            links = listOf(Links.self(Uris.Game.PLAY))
        ).ok()
    }

    /**
     * Handles the request to get the games a user was/is in
     * @param username the user's name (filters games a user was/is in)
     * @param state the game's state (filters games by state)
     * @param page the page number
     * @param limit the max number of games to include
     * @param sort the sort order
     * @return the response with the games
     */
    @GetMapping(Uris.Game.GAMES)
    fun getGames(
        @RequestParam username: String? = null,
        @RequestParam state: String? = null,
        @RequestParam page: Int? = null,
        @RequestParam limit: Int? = null,
        @RequestParam sort: String? = null
    ): ResponseEntity<*> {
        val params = Params(page, limit, sort)
        val (games, totalGames) = services.getGames(
            username = username,
            state = state?.toGameState(),
            skip = params.skip,
            limit = params.limit,
            sort = params.sort
        )
        return SirenEntity(
            clazz = listOf(Rels.Game.GAMES, Rels.Collection.COLLECTION),
            properties = GetGamesCountOutputModel(games.size),
            links = listOfNotNull(
                Links.self(Uris.Game.GAMES),
                params.getPrevPageLink(Uris.Game.games()),
                params.getNextPageLink(Uris.Game.games(), totalGames)
            ),
            entities = games.map { game ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.Game.GAME, Rels.Collection.ITEM),
                    properties = GetGameOutputModel(
                        gameState = game.state.toString(),
                        opponentColor = game.opponent?.toString()
                    ),
                    links = listOf(Links.self(Uris.Game.game(game.gameId).toString())),
                    actions = listOf(Actions.Game.getById(game.gameId)),
                    entities = listOf(
                        SubEntity.EmbeddedRepresentation(
                            rel = listOf(Rels.Game.BLACK_PLAYER),
                            properties = GetUserOutputModel(game.black.name, game.black.email, game.black.stats),
                            links = listOf(Links.self(Uris.User.user(game.black.id).toString()))
                        ),
                        SubEntity.EmbeddedRepresentation(
                            rel = listOf(Rels.Game.WHITE_PLAYER),
                            properties = GetUserOutputModel(game.white.name, game.white.email, game.white.stats),
                            links = listOf(Links.self(Uris.User.user(game.white.id).toString()))
                        )
                    )
                )
            }
        ).ok()
    }

    /**
     * Handles the request to leave a game
     * @param session the user session
     * @return the response
     */
    @PutMapping(Uris.Game.LEAVE)
    fun leaveGame(session: Session): ResponseEntity<*> {
        services.leaveGame(session.user.id)
        return SirenEntity<Unit>(
            clazz = listOf(Rels.Game.LEAVE),
            links = listOf(
                Links.self(Uris.Game.LEAVE),
                Links.home(),
                Links.userHome()
            )
        ).ok()
    }
}
