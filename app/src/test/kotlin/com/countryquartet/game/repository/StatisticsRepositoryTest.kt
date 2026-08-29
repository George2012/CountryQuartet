package com.countryquartet.game.repository

import com.countryquartet.game.model.GameOutcome
import com.countryquartet.game.model.GameStatistics
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsRepositoryTest {

    @Test
    fun `a new device has an empty record`() {
        assertEquals(GameStatistics(), InMemoryStatisticsRepository().statistics.value)
    }

    @Test
    fun `a win is counted once`() {
        val repository = InMemoryStatisticsRepository()

        repository.recordFinishedGame(GameOutcome.WON, quartetsCollected = 5)

        assertEquals(
            GameStatistics(
                gamesPlayed = 1,
                gamesWon = 1,
                totalQuartets = 5,
                bestScore = 5,
            ),
            repository.statistics.value,
        )
    }

    @Test
    fun `wins, losses and draws are counted separately`() {
        val repository = InMemoryStatisticsRepository()

        repository.recordFinishedGame(GameOutcome.WON, 5)
        repository.recordFinishedGame(GameOutcome.LOST, 2)
        repository.recordFinishedGame(GameOutcome.DRAW, 4)
        repository.recordFinishedGame(GameOutcome.LOST, 1)

        val statistics = repository.statistics.value
        assertEquals(4, statistics.gamesPlayed)
        assertEquals(1, statistics.gamesWon)
        assertEquals(2, statistics.gamesLost)
        assertEquals(1, statistics.draws)
        assertEquals(12, statistics.totalQuartets)
    }

    @Test
    fun `the best score only ever goes up`() {
        val repository = InMemoryStatisticsRepository()

        repository.recordFinishedGame(GameOutcome.WON, 6)
        repository.recordFinishedGame(GameOutcome.LOST, 2)

        assertEquals(6, repository.statistics.value.bestScore)
    }

    @Test
    fun `resetting clears everything`() {
        val repository = InMemoryStatisticsRepository()
        repository.recordFinishedGame(GameOutcome.WON, 7)

        repository.reset()

        assertEquals(GameStatistics(), repository.statistics.value)
    }

    @Test
    fun `an existing record is carried forward`() {
        val repository = InMemoryStatisticsRepository(
            GameStatistics(gamesPlayed = 3, gamesWon = 1, totalQuartets = 9, bestScore = 4),
        )

        repository.recordFinishedGame(GameOutcome.WON, 5)

        val statistics = repository.statistics.value
        assertEquals(4, statistics.gamesPlayed)
        assertEquals(2, statistics.gamesWon)
        assertEquals(14, statistics.totalQuartets)
        assertEquals(5, statistics.bestScore)
    }
}
