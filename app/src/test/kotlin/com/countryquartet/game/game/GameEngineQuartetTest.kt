package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineQuartetTest {

    private val engine = TestGame.engine

    @Test
    fun `receiving the fourth card completes the quartet`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk"),
            listOf("fi"),
            listOf("de"),
            listOf("jp"),
        )

        val result = engine.ask(state, "p1", "fi")

        assertEquals("nordic_countries", (result.outcome as RequestOutcome.Success).completedQuartetId)
        assertEquals(listOf("nordic_countries"), result.state.player("p0").completedQuartets)
    }

    @Test
    fun `the four cards leave the active hand`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk", "it"),
            listOf("fi"),
            listOf("de"),
            listOf("jp"),
        )

        val hand = engine.ask(state, "p1", "fi").state.player("p0").cards

        assertEquals(listOf("it"), hand)
        TestGame.cardsOf("nordic_countries").forEach { assertFalse(it in hand) }
    }

    @Test
    fun `completing a quartet raises the score`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk"),
            listOf("fi"),
            listOf("de"),
            listOf("jp"),
        )

        val after = engine.ask(state, "p1", "fi").state

        assertEquals(1, after.player("p0").score)
        assertEquals(1, after.completedQuartetsCount)
    }

    @Test
    fun `an incomplete quartet does not score`() {
        val state = TestGame.stateOf(listOf("se", "no"), listOf("dk"), listOf("de"), listOf("jp"))

        val result = engine.ask(state, "p1", "dk")

        assertEquals(null, (result.outcome as RequestOutcome.Success).completedQuartetId)
        assertEquals(0, result.state.player("p0").score)
        assertEquals(0, result.state.completedQuartetsCount)
    }

    @Test
    fun `quartets dealt complete are laid down before the first turn`() {
        val state = engine.newGame(random = kotlin.random.Random(11))

        state.players.forEach { player ->
            val stillComplete = engine.representedQuartets(player).filter { quartetId ->
                TestGame.cardsOf(quartetId).all { it in player.cards }
            }
            assertEquals("player ${player.id}", emptyList<String>(), stillComplete)
        }
    }

    @Test
    fun `a completed quartet keeps all four cards together`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk"),
            listOf("fi"),
            listOf("de"),
            listOf("jp"),
        )

        val after = engine.ask(state, "p1", "fi").state

        assertEquals(TestGame.cardsOf("nordic_countries").sorted(), TestGame.allCards(after).filter {
            it in TestGame.cardsOf("nordic_countries")
        }.sorted())
        assertTrue("nordic_countries" in after.player("p0").completedQuartets)
    }

    @Test
    fun `no card is lost or duplicated when a quartet completes`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk", "it"),
            listOf("fi", "es"),
            listOf("de"),
            listOf("jp"),
        )
        val before = TestGame.allCards(state).sorted()

        val after = TestGame.allCards(engine.ask(state, "p1", "fi").state).sorted()

        assertEquals(before, after)
    }
}
