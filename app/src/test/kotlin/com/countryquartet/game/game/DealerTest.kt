package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.random.Random

class DealerTest {

    private val deck = Deck.shuffled(TestGame.gameData, Random(1))

    @Test
    fun `every player receives 13 cards`() {
        Dealer.deal(deck, 4).forEach { hand -> assertEquals(13, hand.size) }
    }

    @Test
    fun `all 52 cards are distributed`() {
        val dealt = Dealer.deal(deck, 4).flatten()
        assertEquals(52, dealt.size)
    }

    @Test
    fun `no card is dealt twice`() {
        val dealt = Dealer.deal(deck, 4).flatten()
        assertEquals(dealt.size, dealt.distinct().size)
        assertEquals(deck.toSet(), dealt.toSet())
    }

    @Test
    fun `an uneven deck is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { Dealer.deal(deck.drop(1), 4) }
    }
}
