package com.countryquartet.game.ai

import com.countryquartet.game.game.TestGame
import com.countryquartet.game.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Phase 4 acceptance run: complete AI-vs-AI-vs-AI-vs-AI games.
 *
 * [GameSimulator] verifies the table after every move, so these tests fail at
 * the exact move that loses a card rather than at the end of the game.
 */
class AiSimulationTest {

    private val simulator = GameSimulator(TestGame.engine, TestGame.gameData)

    @Test
    fun `one hundred simulated games all finish correctly`() {
        val results = (1L..GAMES).map { simulator.play(it) }

        results.forEach { result ->
            val where = "seed ${result.seed}"
            assertEquals(where, GameStatus.FINISHED, result.finalState.status)
            assertEquals(where, 13, result.finalState.completedQuartetsCount)
            assertEquals(where, 13, result.finalState.players.sumOf { it.score })
            assertEquals(where, emptyList<String>(), result.finalState.players.flatMap { it.cards })
            assertTrue(where, result.finalState.winnerIds.isNotEmpty())
        }
        assertEquals(GAMES.toInt(), results.size)
    }

    @Test
    fun `no simulated game loses or duplicates a card`() {
        (1L..GAMES).forEach { seed ->
            val cards = TestGame.allCards(simulator.play(seed).finalState)

            assertEquals("seed $seed", 52, cards.size)
            assertEquals("seed $seed", 52, cards.distinct().size)
            assertEquals("seed $seed", TestGame.gameData.countryIds.toSet(), cards.toSet())
        }
    }

    @Test
    fun `every simulated game terminates well inside the move limit`() {
        val moves = (1L..GAMES).map { simulator.play(it).moves }

        assertTrue(moves.toString(), moves.all { it in 1 until GameSimulator.MOVE_LIMIT })
        // A game that needed anywhere near the limit would mean the AI is
        // spinning rather than collecting.
        assertTrue("longest game took ${moves.max()} moves", moves.max() < 1_000)
    }

    @Test
    fun `every game runs the draw pile down to empty`() {
        // If the pile never emptied, the rules for playing on without it would
        // never be exercised by these simulations.
        val leftover = (1L..GAMES).map { simulator.play(it).finalState.deckCount }

        assertEquals("some games ended with cards still in the pile", listOf(0), leftover.distinct())
    }

    @Test
    fun `winners are always the players with the highest score`() {
        (1L..GAMES).forEach { seed ->
            val state = simulator.play(seed).finalState
            val best = state.players.maxOf { it.score }

            assertEquals(
                "seed $seed",
                state.players.filter { it.score == best }.map { it.id },
                state.winnerIds,
            )
        }
    }

    companion object {
        /** Phase 4 requires at least 100 complete games. */
        const val GAMES = 100L
    }
}
