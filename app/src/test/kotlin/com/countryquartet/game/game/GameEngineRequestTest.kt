package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineRequestTest {

    private val engine = TestGame.engine

    @Test
    fun `a successful request transfers the card`() {
        val state = TestGame.stateOf(
            listOf("se", "it"),
            listOf("no", "es"),
            listOf("de"),
            listOf("jp"),
        )

        val result = engine.ask(state, targetPlayerId = "p1", countryId = "no")

        assertTrue("no" in result.state.player("p0").cards)
        assertTrue("no" !in result.state.player("p1").cards)
        assertEquals(3, result.state.player("p0").cards.size)
        assertEquals(1, result.state.player("p1").cards.size)
    }

    @Test
    fun `a successful request reports who gave what`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val outcome = engine.ask(state, "p1", "no").outcome

        assertEquals(RequestOutcome.Success("p0", "p1", "no", completedQuartetId = null), outcome)
    }

    @Test
    fun `a failed request leaves every hand untouched`() {
        val state = TestGame.stateOf(listOf("se"), listOf("es"), listOf("de"), listOf("jp"))

        val result = engine.ask(state, targetPlayerId = "p1", countryId = "no")

        assertEquals(RequestOutcome.Failure("p0", "p1", "no"), result.outcome)
        assertEquals(listOf("se"), result.state.player("p0").cards)
        assertEquals(listOf("es"), result.state.player("p1").cards)
    }

    @Test
    fun `requesting an unknown country is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) {
            engine.ask(state, "p1", "atlantis")
        }
        assertTrue(error.message!!, error.message!!.contains("Unknown country"))
    }

    @Test
    fun `requesting a card you already own is rejected`() {
        val state = TestGame.stateOf(listOf("se", "no"), listOf("dk"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) { engine.ask(state, "p1", "no") }
        assertTrue(error.message!!, error.message!!.contains("already owns"))
    }

    @Test
    fun `requesting from a quartet you hold no card of is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("jp"), listOf("de"), listOf("cn"))

        val error = assertThrows(IllegalMoveException::class.java) { engine.ask(state, "p1", "jp") }
        assertTrue(error.message!!, error.message!!.contains("holds no card of quartet"))
    }

    @Test
    fun `requesting a card of an already completed quartet is rejected`() {
        val state = TestGame.stateOf(
            listOf("se", "it"),
            listOf("es"),
            listOf("de"),
            listOf("jp"),
            completed = listOf(emptyList(), emptyList(), listOf("nordic_countries"), emptyList()),
        )

        val error = assertThrows(IllegalMoveException::class.java) { engine.ask(state, "p1", "no") }
        assertTrue(error.message!!, error.message!!.contains("already completed"))
    }

    @Test
    fun `asking yourself is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) { engine.ask(state, "p0", "no") }
        assertTrue(error.message!!, error.message!!.contains("cannot ask themselves"))
    }

    @Test
    fun `asking an unknown player is rejected`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val error = assertThrows(IllegalMoveException::class.java) { engine.ask(state, "p9", "no") }
        assertTrue(error.message!!, error.message!!.contains("Unknown player"))
    }

    @Test
    fun `legal requests cover every missing country of every represented quartet`() {
        val state = TestGame.stateOf(listOf("se", "it"), listOf("no"), listOf("de"), listOf("jp"))

        val requests = engine.legalRequests(state)

        val wanted = requests.filter { it.targetPlayerId == "p1" }.map { it.countryId }.toSet()
        assertEquals(setOf("no", "dk", "fi", "es", "pt", "gr"), wanted)
        assertEquals(setOf("p1", "p2", "p3"), requests.map { it.targetPlayerId }.toSet())
    }

    @Test
    fun `every listed request is accepted by the engine`() {
        val state = TestGame.stateOf(listOf("se", "it"), listOf("no"), listOf("de"), listOf("jp"))

        engine.legalRequests(state).forEach { request ->
            assertTrue(
                "$request should be legal",
                engine.isLegalRequest(state, request.targetPlayerId, request.countryId),
            )
        }
    }

    @Test
    fun `a finished game accepts no more requests`() {
        val finished = GameRunner.playToEnd(engine, engine.newGame(random = Random(3)))

        assertEquals(emptyList<CardRequest>(), engine.legalRequests(finished))
        val error = assertThrows(IllegalMoveException::class.java) {
            engine.ask(finished, "ai_1", "se")
        }
        assertTrue(error.message!!, error.message!!.contains("already finished"))
    }
}
