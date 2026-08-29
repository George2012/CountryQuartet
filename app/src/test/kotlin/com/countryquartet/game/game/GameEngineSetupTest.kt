package com.countryquartet.game.game

import com.countryquartet.game.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineSetupTest {

    private val engine = TestGame.engine

    @Test
    fun `a new game has four players, one of them human`() {
        val state = engine.newGame(random = Random(5))

        assertEquals(4, state.players.size)
        assertEquals(1, state.players.count { it.isHuman })
        assertEquals(GameEngine.HUMAN_ID, state.players.first { it.isHuman }.id)
    }

    @Test
    fun `a new game deals all 52 cards`() {
        val state = engine.newGame(random = Random(5))
        val cards = TestGame.allCards(state)

        assertEquals(52, cards.size)
        assertEquals(52, cards.distinct().size)
    }

    @Test
    fun `each player starts with 13 cards unless a quartet was dealt complete`() {
        val state = engine.newGame(random = Random(5))

        state.players.forEach { player ->
            assertEquals(
                "player ${player.id}",
                GameEngine.CARDS_PER_PLAYER,
                player.cards.size + player.completedQuartets.size * 4,
            )
        }
    }

    @Test
    fun `a new game starts with the human player and no winner`() {
        val state = engine.newGame(random = Random(5))

        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertEquals(emptyList<String>(), state.winnerIds)
        assertTrue(state.currentPlayer.cards.isNotEmpty())
    }

    @Test
    fun `the same seed deals the same game`() {
        val first = engine.newGame(random = Random(99))
        val second = engine.newGame(random = Random(99))

        assertEquals(first, second)
    }

    @Test
    fun `custom seats are used`() {
        val seats = listOf(
            PlayerSeat("a", "Ada", isHuman = true),
            PlayerSeat("b", "Bo", isHuman = false),
            PlayerSeat("c", "Cy", isHuman = false),
            PlayerSeat("d", "Dee", isHuman = false),
        )

        val state = engine.newGame(seats, Random(5))

        assertEquals(listOf("a", "b", "c", "d"), state.players.map { it.id })
        assertEquals(listOf("Ada", "Bo", "Cy", "Dee"), state.players.map { it.name })
    }

    @Test
    fun `the wrong number of players is rejected`() {
        val seats = GameEngine.defaultSeats().drop(1)

        assertThrows(IllegalArgumentException::class.java) { engine.newGame(seats, Random(5)) }
    }

    @Test
    fun `duplicate player ids are rejected`() {
        val seats = List(4) { PlayerSeat("same", "Same", isHuman = it == 0) }

        assertThrows(IllegalArgumentException::class.java) { engine.newGame(seats, Random(5)) }
    }
}
