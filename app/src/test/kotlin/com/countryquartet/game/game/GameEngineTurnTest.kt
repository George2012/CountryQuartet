package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTurnTest {

    private val engine = TestGame.engine

    @Test
    fun `a successful request keeps the turn`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("de"), listOf("jp"))

        val after = engine.ask(state, "p1", "no").state

        assertEquals(0, after.currentPlayerIndex)
        assertEquals("p0", after.currentPlayer.id)
    }

    @Test
    fun `completing a quartet keeps the turn`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk", "it"),
            listOf("fi"),
            listOf("de"),
            listOf("jp"),
        )

        assertEquals(0, engine.ask(state, "p1", "fi").state.currentPlayerIndex)
    }

    @Test
    fun `a failed request passes the turn on`() {
        val state = TestGame.stateOf(listOf("se"), listOf("es"), listOf("de"), listOf("jp"))

        assertEquals(1, engine.ask(state, "p1", "no").state.currentPlayerIndex)
    }

    @Test
    fun `the turn wraps around to the first player`() {
        val state = TestGame.stateOf(
            listOf("se"),
            listOf("no"),
            listOf("dk"),
            listOf("fi"),
            currentPlayerIndex = 3,
        )

        assertEquals(0, engine.ask(state, "p0", "no").state.currentPlayerIndex)
    }

    @Test
    fun `players without cards are skipped`() {
        val state = TestGame.stateOf(
            listOf("se"),
            emptyList(),
            emptyList(),
            listOf("no"),
            completed = listOf(emptyList(), listOf("east_asia"), listOf("south_asia"), emptyList()),
        )

        assertEquals(3, engine.ask(state, "p3", "dk").state.currentPlayerIndex)
    }

    @Test
    fun `a player who runs out of cards hands the turn over`() {
        // p0 completes its last quartet and is left with an empty hand.
        val state = TestGame.stateOf(
            listOf("se", "no", "dk"),
            listOf("fi", "it"),
            listOf("de"),
            listOf("jp"),
        )

        val after = engine.ask(state, "p1", "fi").state

        assertEquals(emptyList<String>(), after.player("p0").cards)
        assertEquals(1, after.currentPlayerIndex)
    }
}
