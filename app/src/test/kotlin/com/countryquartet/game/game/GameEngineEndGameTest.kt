package com.countryquartet.game.game

import com.countryquartet.game.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineEndGameTest {

    private val engine = TestGame.engine

    /** Twelve quartets already collected, the thirteenth one card away. */
    private fun almostFinished(
        completed: List<List<String>>,
        hands: List<List<String>>,
        currentPlayerIndex: Int,
    ) = TestGame.stateOf(
        hands[0], hands[1], hands[2], hands[3],
        currentPlayerIndex = currentPlayerIndex,
        completed = completed,
    )

    @Test
    fun `the game ends when all 13 quartets are completed`() {
        val state = almostFinished(
            completed = listOf(
                listOf("nordic_countries", "southern_europe", "central_europe", "eastern_europe", "middle_east"),
                listOf("east_asia", "south_asia", "southeast_asia", "north_africa"),
                listOf("east_africa", "north_america", "south_america"),
                emptyList(),
            ),
            hands = listOf(listOf("au", "nz", "pg"), listOf("fj"), emptyList(), emptyList()),
            currentPlayerIndex = 0,
        )

        val after = engine.ask(state, "p1", "fj").state

        assertEquals(GameStatus.FINISHED, after.status)
        assertEquals(13, after.completedQuartetsCount)
        assertTrue(after.isFinished)
    }

    @Test
    fun `the player with the most quartets wins`() {
        val state = almostFinished(
            completed = listOf(
                listOf("nordic_countries", "southern_europe", "central_europe", "eastern_europe", "middle_east"),
                listOf("east_asia", "south_asia", "southeast_asia", "north_africa"),
                listOf("east_africa", "north_america", "south_america"),
                emptyList(),
            ),
            hands = listOf(listOf("au", "nz", "pg"), listOf("fj"), emptyList(), emptyList()),
            currentPlayerIndex = 0,
        )

        val after = engine.ask(state, "p1", "fj").state

        assertEquals(listOf("p0"), after.winnerIds)
        assertEquals(listOf(6, 4, 3, 0), after.players.map { it.score })
    }

    @Test
    fun `an equal number of quartets is a draw`() {
        val state = almostFinished(
            completed = listOf(
                listOf("nordic_countries", "southern_europe", "central_europe", "eastern_europe", "middle_east"),
                listOf("east_asia", "south_asia", "southeast_asia", "north_africa", "east_africa"),
                listOf("north_america", "south_america"),
                emptyList(),
            ),
            hands = listOf(emptyList(), emptyList(), listOf("au", "nz", "pg"), listOf("fj")),
            currentPlayerIndex = 2,
        )

        val after = engine.ask(state, "p3", "fj").state

        assertEquals(GameStatus.FINISHED, after.status)
        assertEquals(listOf("p0", "p1"), after.winnerIds)
        assertEquals(listOf(5, 5, 3, 0), after.players.map { it.score })
    }

    @Test
    fun `a running game has no winner yet`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertEquals(emptyList<String>(), state.winnerIds)
        assertEquals(emptyList<String>(), engine.ask(state, "p1", "no").state.winnerIds)
    }

    @Test
    fun `played games always end with 13 quartets and every card accounted for`() {
        repeat(20) { seed ->
            val start = engine.newGame(random = Random(seed.toLong()))
            val finished = GameRunner.playToEnd(engine, start, Random(seed.toLong()))

            assertEquals("seed $seed", GameStatus.FINISHED, finished.status)
            assertEquals("seed $seed", 13, finished.completedQuartetsCount)
            assertEquals("seed $seed", 13, finished.players.sumOf { it.score })
            assertEquals("seed $seed", emptyList<String>(), finished.players.flatMap { it.cards })

            val cards = TestGame.allCards(finished)
            assertEquals("seed $seed", 52, cards.size)
            assertEquals("seed $seed", 52, cards.distinct().size)
            assertTrue("seed $seed", finished.winnerIds.isNotEmpty())
        }
    }
}
