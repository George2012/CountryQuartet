package com.countryquartet.game.ai

import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.game.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BasicAiTest {

    private val engine = TestGame.engine

    @Test
    fun `it works on the quartet it holds the most cards of`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk", "it", "de"),
            listOf("fi", "es"),
            listOf("at"),
            listOf("jp"),
        )

        repeat(30) { seed ->
            val request = BasicAi(Random(seed.toLong())).chooseRequest(engine, state)
            assertEquals("seed $seed", "fi", request.countryId)
        }
    }

    @Test
    fun `it picks among the missing countries when quartets are equally strong`() {
        val state = TestGame.stateOf(
            listOf("se", "it"),
            listOf("no", "es"),
            listOf("de"),
            listOf("jp"),
        )

        val asked = (0 until 60).map {
            BasicAi(Random(it.toLong())).chooseRequest(engine, state).countryId
        }.toSet()

        assertTrue(asked.isNotEmpty())
        assertTrue(asked.toString(), asked.all { it in setOf("no", "dk", "fi", "es", "pt", "gr") })
    }

    @Test
    fun `it never asks for a card it already owns`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "it"),
            listOf("dk", "es"),
            listOf("fi"),
            listOf("jp"),
        )

        repeat(60) { seed ->
            val request = BasicAi(Random(seed.toLong())).chooseRequest(engine, state)
            assertFalse("seed $seed", request.countryId in state.player("p0").cards)
        }
    }

    @Test
    fun `it never asks itself`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("dk"), listOf("fi"))

        repeat(60) { seed ->
            val request = BasicAi(Random(seed.toLong())).chooseRequest(engine, state)
            assertNotEquals("seed $seed", "p0", request.targetPlayerId)
        }
    }

    @Test
    fun `it skips opponents that ran out of cards`() {
        val state = TestGame.stateOf(
            listOf("se", "no"),
            emptyList(),
            emptyList(),
            listOf("dk"),
            completed = listOf(emptyList(), listOf("east_asia"), listOf("south_asia"), emptyList()),
        )

        repeat(60) { seed ->
            val request = BasicAi(Random(seed.toLong())).chooseRequest(engine, state)
            assertEquals("seed $seed", "p3", request.targetPlayerId)
        }
    }

    @Test
    fun `it only proposes requests the engine accepts`() {
        var state = engine.newGame(GameSimulator.AI_ONLY_SEATS, Random(4))
        val ai = BasicAi(Random(4))

        repeat(400) {
            if (state.isFinished) return@repeat
            val request = ai.chooseRequest(engine, state)
            assertTrue(
                "$request rejected by the engine",
                engine.isLegalRequest(state, request.targetPlayerId, request.countryId),
            )
            state = engine.ask(state, request.targetPlayerId, request.countryId).state
        }
    }

    @Test
    fun `it can finish a game`() {
        val result = GameSimulator(engine, TestGame.gameData).play(seed = 1)

        assertTrue(result.finalState.isFinished)
        assertEquals(13, result.finalState.completedQuartetsCount)
    }

    @Test
    fun `the same seed produces the same game`() {
        val engine = GameEngine(TestGame.gameData)
        val first = GameSimulator(engine, TestGame.gameData).play(seed = 77)
        val second = GameSimulator(engine, TestGame.gameData).play(seed = 77)

        assertEquals(first.moves, second.moves)
        assertEquals(first.finalState, second.finalState)
    }
}
