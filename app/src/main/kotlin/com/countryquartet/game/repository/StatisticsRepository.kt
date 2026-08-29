package com.countryquartet.game.repository

import com.countryquartet.game.model.GameOutcome
import com.countryquartet.game.model.GameStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Keeps the lifetime record of games played on this device.
 *
 * Nothing leaves the device: there are no accounts and no synchronisation.
 */
interface StatisticsRepository {

    val statistics: StateFlow<GameStatistics>

    fun recordFinishedGame(outcome: GameOutcome, quartetsCollected: Int)

    fun reset()
}

/** Used by the tests and as the fallback before storage is available. */
class InMemoryStatisticsRepository(
    initial: GameStatistics = GameStatistics(),
) : StatisticsRepository {

    private val _statistics = MutableStateFlow(initial)
    override val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    override fun recordFinishedGame(outcome: GameOutcome, quartetsCollected: Int) {
        _statistics.update { it.recording(outcome, quartetsCollected) }
    }

    override fun reset() {
        _statistics.value = GameStatistics()
    }
}
