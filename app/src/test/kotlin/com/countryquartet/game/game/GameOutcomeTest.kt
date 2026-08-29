package com.countryquartet.game.game

import com.countryquartet.game.model.GameOutcome
import com.countryquartet.game.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameOutcomeTest {

    private fun finished(winners: List<String>) = TestGame
        .stateOf(listOf("se"), listOf("no"), listOf("dk"), listOf("fi"))
        .copy(status = GameStatus.FINISHED, winnerIds = winners)

    @Test
    fun `a running game has no outcome yet`() {
        val running = TestGame.stateOf(listOf("se"), listOf("no"), listOf("dk"), listOf("fi"))

        assertNull(running.outcomeFor("p0"))
    }

    @Test
    fun `the sole winner won`() {
        assertEquals(GameOutcome.WON, finished(listOf("p0")).outcomeFor("p0"))
    }

    @Test
    fun `everyone else lost`() {
        val state = finished(listOf("p0"))

        assertEquals(GameOutcome.LOST, state.outcomeFor("p1"))
        assertEquals(GameOutcome.LOST, state.outcomeFor("p2"))
    }

    @Test
    fun `sharing the top score is a draw, not a win`() {
        val state = finished(listOf("p0", "p2"))

        assertEquals(GameOutcome.DRAW, state.outcomeFor("p0"))
        assertEquals(GameOutcome.DRAW, state.outcomeFor("p2"))
        assertEquals(GameOutcome.LOST, state.outcomeFor("p1"))
    }
}
