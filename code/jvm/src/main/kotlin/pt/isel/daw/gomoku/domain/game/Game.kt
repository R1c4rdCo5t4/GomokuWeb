package pt.isel.daw.gomoku.domain.game

/**
 * Represents a game
 * @property id the id of the game
 * @property board the board of the game
 * @property blackPlayerId the id of the user playing with the black pieces
 * @property whitePlayerId the id of the user playing with the white pieces
 * @property config the config of the game
 * @property turn the turn of the game
 * @property state the state of the game
 */
data class Game(
    val id: Int,
    val board: Board,
    val blackPlayerId: Int,
    val whitePlayerId: Int,
    val config: GameConfig,
    val turn: Turn = Turn.BLACK,
    val state: GameState = GameState.RUNNING
) {
    init {
        require(id > 0) { "Game id must be positive" }
        require(board.size in listOf(15, 19)) { "Game board must have size 15 or 19" }
        require(config.boardSize in listOf(15, 19)) { "Game board size in game config must have size 15 or 19" }
        require(board.size == config.boardSize) { "Game board size and game config board size must match" }
    }

    val isOver: Boolean get() = state != GameState.RUNNING
}
