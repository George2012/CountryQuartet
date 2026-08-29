package com.countryquartet.game.model

/** Whether a game is still being played or has ended. */
enum class GameStatus { IN_PROGRESS, FINISHED }

/**
 * The complete state of one game. Immutable: the engine returns a new state for
 * every move, which keeps the UI layer free of hidden mutations.
 *
 * [winnerIds] is empty while the game runs and holds every winner once it is
 * finished, so a draw is simply more than one id.
 */
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val status: GameStatus,
    val winnerIds: List<String>,
) {
    /**
     * Number of quartets finished by all players together.
     *
     * Derived rather than stored, so it can never drift away from the players.
     */
    val completedQuartetsCount: Int = players.sumOf { it.completedQuartets.size }

    val currentPlayer: Player get() = players[currentPlayerIndex]

    val isFinished: Boolean get() = status == GameStatus.FINISHED

    fun player(id: String): Player =
        players.firstOrNull { it.id == id } ?: throw IllegalArgumentException("Unknown player id: $id")

    fun playerOrNull(id: String): Player? = players.firstOrNull { it.id == id }
}
