package com.countryquartet.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DealerTest {

    private val deck = Deck.shuffled(TestGame.gameData, Random(1))

    @Test
    fun `every player receives the requested number of cards`() {
        Dealer.deal(deck, playerCount = 4, cardsPerPlayer = 5).hands.forEach { hand ->
            assertEquals(5, hand.size)
        }
    }

    @Test
    fun `the undealt cards become the draw pile`() {
        val dealt = Dealer.deal(deck, playerCount = 4, cardsPerPlayer = 5)

        assertEquals(52 - 4 * 5, dealt.deck.size)
        assertEquals(52, dealt.hands.flatten().size + dealt.deck.size)
    }

    @Test
    fun `no card is dealt twice or lost`() {
        val dealt = Dealer.deal(deck, playerCount = 4, cardsPerPlayer = 5)
        val all = dealt.hands.flatten() + dealt.deck

        assertEquals(all.size, all.distinct().size)
        assertEquals(deck.toSet(), all.toSet())
    }

    @Test
    fun `hands and pile do not overlap`() {
        val dealt = Dealer.deal(deck, playerCount = 4, cardsPerPlayer = 5)
        val inHands = dealt.hands.flatten().toSet()

        assertTrue(dealt.deck.none { it in inHands })
    }

    @Test
    fun `the hand size can be changed`() {
        val dealt = Dealer.deal(deck, playerCount = 4, cardsPerPlayer = 13)

        dealt.hands.forEach { assertEquals(13, it.size) }
        assertEquals(emptyList<String>(), dealt.deck)
    }

    @Test
    fun `dealing more cards than the deck holds is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Dealer.deal(deck, playerCount = 4, cardsPerPlayer = 14)
        }
    }
}
