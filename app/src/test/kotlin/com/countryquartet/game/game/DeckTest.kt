package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class DeckTest {

    private val gameData = TestGame.gameData

    @Test
    fun `deck contains 52 cards`() {
        assertEquals(52, Deck.create(gameData).size)
    }

    @Test
    fun `deck contains no duplicates`() {
        val deck = Deck.create(gameData)
        assertEquals(deck.size, deck.distinct().size)
    }

    @Test
    fun `deck holds every country of the dataset`() {
        assertEquals(gameData.countries.map { it.id }.toSet(), Deck.create(gameData).toSet())
    }

    @Test
    fun `shuffling keeps every card exactly once`() {
        val shuffled = Deck.shuffled(gameData, Random(7))
        assertEquals(52, shuffled.size)
        assertEquals(Deck.create(gameData).toSet(), shuffled.toSet())
    }

    @Test
    fun `shuffling actually changes the order`() {
        assertNotEquals(Deck.create(gameData), Deck.shuffled(gameData, Random(7)))
    }

    @Test
    fun `the same seed produces the same deck`() {
        assertEquals(Deck.shuffled(gameData, Random(42)), Deck.shuffled(gameData, Random(42)))
    }
}
