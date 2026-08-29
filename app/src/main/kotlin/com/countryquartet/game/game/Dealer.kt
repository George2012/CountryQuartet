package com.countryquartet.game.game

/** Splits a deck evenly between the players. */
object Dealer {

    /**
     * Deals [cards] round-robin into [playerCount] hands.
     *
     * @throws IllegalArgumentException if the deck cannot be divided evenly.
     */
    fun deal(cards: List<String>, playerCount: Int): List<List<String>> {
        require(playerCount > 0) { "A game needs at least one player" }
        require(cards.size % playerCount == 0) {
            "${cards.size} cards cannot be dealt evenly to $playerCount players"
        }
        val hands = List(playerCount) { mutableListOf<String>() }
        cards.forEachIndexed { index, card -> hands[index % playerCount] += card }
        return hands.map { it.toList() }
    }
}
