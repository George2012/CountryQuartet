package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineRegionRequestTest {

    private val engine = TestGame.engine

    @Test
    fun `a present region leaves every hand untouched and keeps the turn`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val result = engine.askRegion(state, targetPlayerId = "p1", quartetId = "nordic_countries")

        assertEquals(
            RegionOutcome.Present("p0", "p1", "nordic_countries"),
            result.outcome,
        )
        assertEquals(listOf("se"), result.state.player("p0").cards)
        assertEquals(listOf("no"), result.state.player("p1").cards)
        assertEquals(0, result.state.currentPlayerIndex)
    }

    @Test
    fun `an absent region ends the turn like a failed request`() {
        val state = TestGame.stateOf(listOf("se"), listOf("es"), listOf("de"), listOf("jp"))

        val result = engine.askRegion(state, targetPlayerId = "p1", quartetId = "nordic_countries")

        assertEquals(
            RegionOutcome.Absent("p0", "p1", "nordic_countries"),
            result.outcome,
        )
        assertEquals(listOf("se"), result.state.player("p0").cards)
        assertEquals(1, result.state.currentPlayerIndex)
    }

    @Test
    fun `an absent region draws a consolation card when the deck has one`() {
        val state = TestGame.stateOf(
            listOf("se"), listOf("es"), listOf("de"), listOf("jp"),
            deck = listOf("no"),
        )

        val result = engine.askRegion(state, targetPlayerId = "p1", quartetId = "nordic_countries")

        val outcome = result.outcome as RegionOutcome.Absent
        assertTrue(outcome.drewFromDeck)
        assertEquals(listOf("se", "no"), result.state.player("p0").cards)
        assertEquals(0, result.state.deckCount)
    }

    @Test
    fun `asking about an unknown quartet is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.askRegion(state, "p1", "atlantis")
        }
        assertTrue(error.message!!, error.message!!.contains("Unknown quartet"))
    }

    @Test
    fun `asking about a quartet you hold no card of is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("jp"), listOf("de"), listOf("cn"))

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.askRegion(state, "p1", "east_asia")
        }
        assertTrue(error.message!!, error.message!!.contains("holds no card of quartet"))
    }

    @Test
    fun `asking about an already completed quartet is rejected`() {
        val state = TestGame.stateOf(
            listOf("se", "it"),
            listOf("es"),
            listOf("de"),
            listOf("jp"),
            completed = listOf(emptyList(), emptyList(), listOf("nordic_countries"), emptyList()),
        )

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.askRegion(state, "p1", "nordic_countries")
        }
        assertTrue(error.message!!, error.message!!.contains("already completed"))
    }

    @Test
    fun `asking yourself about a region is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.askRegion(state, "p0", "nordic_countries")
        }
        assertTrue(error.message!!, error.message!!.contains("cannot ask themselves"))
    }

    @Test
    fun `asking an unknown player about a region is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.askRegion(state, "p9", "nordic_countries")
        }
        assertTrue(error.message!!, error.message!!.contains("Unknown player"))
    }

    @Test
    fun `a finished game accepts no more region requests`() {
        val finished = GameRunner.playToEnd(engine, engine.newGame(random = kotlin.random.Random(3)))

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.askRegion(finished, "ai_1", "nordic_countries")
        }
        assertTrue(error.message!!, error.message!!.contains("already finished"))
    }

    @Test
    fun `isLegalRegionRequest agrees with askRegion`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        assertTrue(engine.isLegalRegionRequest(state, "p1", "nordic_countries"))
        assertTrue(!engine.isLegalRegionRequest(state, "p0", "nordic_countries"))
        assertTrue(!engine.isLegalRegionRequest(state, "p1", "east_asia"))
    }
}
