package com.countryquartet.game.model

/** How a finished game ended for one player. */
enum class GameOutcome { WON, LOST, DRAW }

/**
 * Lifetime record of the games played on this device.
 *
 * Pure data with a pure update rule, so the counting is covered by plain JVM
 * tests no matter where the numbers are stored.
 */
data class GameStatistics(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val draws: Int = 0,
    val totalQuartets: Int = 0,
    val bestScore: Int = 0,
) {
    /** The statistics after finishing one more game. */
    fun recording(outcome: GameOutcome, quartetsCollected: Int): GameStatistics = copy(
        gamesPlayed = gamesPlayed + 1,
        gamesWon = gamesWon + if (outcome == GameOutcome.WON) 1 else 0,
        gamesLost = gamesLost + if (outcome == GameOutcome.LOST) 1 else 0,
        draws = draws + if (outcome == GameOutcome.DRAW) 1 else 0,
        totalQuartets = totalQuartets + quartetsCollected,
        bestScore = maxOf(bestScore, quartetsCollected),
    )
}
