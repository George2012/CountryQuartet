package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The draw pile rules: losing a turn costs a card, and empty hands are topped up. */
class GameEngineDeckTest {

    private val engine = TestGame.engine

    @Test
    fun `a failed request takes one card from the pile`() {
        val state = TestGame.stateOf(
            listOf("se"), listOf("es"), listOf("de"), listOf("jp"),
            deck = listOf("no", "dk"),
        )

        val result = engine.ask(state, "p1", "no")

        assertEquals(listOf("se", "no"), result.state.player("p0").cards)
        assertEquals(1, result.state.deckCount)
        assertTrue((result.outcome as RequestOutcome.Failure).drewFromDeck)
    }

    @Test
    fun `a failed request names the card it took off the pile`() {
        val state = TestGame.stateOf(
            listOf("se"), listOf("es"), listOf("de"), listOf("jp"),
            deck = listOf("no", "dk"),
        )

        val outcome = engine.ask(state, "p1", "no").outcome as RequestOutcome.Failure

        assertEquals("no", outcome.drewCountryId)
    }

    @Test
    fun `nothing is named when the pile is empty`() {
        val state = TestGame.stateOf(listOf("se"), listOf("es"), listOf("de"), listOf("jp"))

        val outcome = engine.ask(state, "p1", "no").outcome as RequestOutcome.Failure

        assertEquals(null, outcome.drewCountryId)
    }

    @Test
    fun `a failed request still ends the turn`() {
        val state = TestGame.stateOf(
            listOf("se"), listOf("es"), listOf("de"), listOf("jp"),
            deck = listOf("no"),
        )

        assertEquals(1, engine.ask(state, "p1", "no").state.currentPlayerIndex)
    }

    @Test
    fun `nothing is drawn once the pile is empty`() {
        val state = TestGame.stateOf(listOf("se"), listOf("es"), listOf("de"), listOf("jp"))

        val result = engine.ask(state, "p1", "no")

        assertEquals(listOf("se"), result.state.player("p0").cards)
        assertEquals(0, result.state.deckCount)
        assertFalse((result.outcome as RequestOutcome.Failure).drewFromDeck)
        assertEquals(1, result.state.currentPlayerIndex)
    }

    @Test
    fun `a drawn card can complete a quartet`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "dk", "it"), listOf("es"), listOf("de"), listOf("jp"),
            deck = listOf("fi"),
        )

        // Southern Europe is represented by Italy, so asking for Greece is legal.
        val after = engine.ask(state, "p1", "gr").state

        assertEquals(listOf("nordic_countries"), after.player("p0").completedQuartets)
        assertEquals(listOf("it"), after.player("p0").cards)
        assertEquals(1, after.player("p0").score)
    }

    @Test
    fun `an empty hand draws one card and the turn passes on`() {
        val state = TestGame.stateOf(
            listOf("se"), emptyList(), listOf("de"), listOf("jp"),
            deck = listOf("no", "dk", "fi"),
        )

        // p0 asks p2, who does not hold it, so p0 draws and the turn moves to p1,
        // who is empty handed and must draw before passing it on again.
        val after = engine.ask(state, "p2", "no").state

        assertEquals(listOf("se", "no"), after.player("p0").cards)
        assertEquals(listOf("dk"), after.player("p1").cards)
        assertEquals(2, after.currentPlayerIndex)
        assertEquals(1, after.deckCount)
    }

    @Test
    fun `an empty hand is skipped when the pile is gone`() {
        val state = TestGame.stateOf(listOf("se"), emptyList(), listOf("de"), listOf("jp"))

        val after = engine.ask(state, "p2", "no").state

        assertEquals(emptyList<String>(), after.player("p1").cards)
        assertEquals(2, after.currentPlayerIndex)
    }

    @Test
    fun `the player to move always holds cards while the game runs`() {
        val state = TestGame.stateOf(
            listOf("se"), emptyList(), emptyList(), listOf("jp"),
            deck = emptyList(),
        )

        val after = engine.ask(state, "p3", "no").state

        assertFalse(after.isFinished)
        assertTrue(after.currentPlayer.cards.isNotEmpty())
        assertEquals(3, after.currentPlayerIndex)
    }

    @Test
    fun `no card is lost between hands and the pile`() {
        val state = TestGame.stateOf(
            listOf("se", "it"), listOf("es"), listOf("de"), listOf("jp"),
            deck = listOf("no", "dk", "fi"),
        )
        val before = TestGame.allCards(state).sorted()

        val after = TestGame.allCards(engine.ask(state, "p1", "no").state).sorted()

        assertEquals(before, after)
    }
}
