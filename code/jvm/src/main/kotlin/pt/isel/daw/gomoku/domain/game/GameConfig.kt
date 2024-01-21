package pt.isel.daw.gomoku.domain.game

/**
 * Represents the configuration of a game
 * @property boardSize the size of the board
 * @property variant the variant of the game
 * @property opening the opening of the game
 */
data class GameConfig(val boardSize: Int, val variant: Variant, val opening: Opening) {

    init {
        require(boardSize in listOf(15, 19)) { "Board size must be either 15 or 19" }
    }

    constructor(boardSize: Int, variantStr: String, openingStr: String) :
        this(boardSize, Rule.to<Variant>(variantStr), Rule.to<Opening>(openingStr))

    companion object {
        const val OPENING_MOVES = 2
    }
}
