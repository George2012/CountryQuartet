package com.countryquartet.game.game

/** The result of dealing: one hand per player and whatever is left to draw from. */
data class DealtCards(
    val hands: List<List<String>>,
    val deck: List<String>,
)

/** Deals the opening hands and leaves the rest of the deck as a draw pile. */
object Dealer {

    /**
     * Deals [cardsPerPlayer] cards to each of [playerCount] players, one card at
     * a time in turn, and returns the undealt remainder as the draw pile.
     *
     * @throws IllegalArgumentException if there are not enough cards.
     */
    fun deal(cards: List<String>, playerCount: Int, cardsPerPlayer: Int): DealtCards {
        require(playerCount > 0) { "A game needs at least one player" }
        require(cardsPerPlayer > 0) { "Every player needs at least one card" }
        val needed = playerCount * cardsPerPlayer
        require(cards.size >= needed) {
            "$needed cards are needed for $playerCount players but only ${cards.size} are available"
        }

        val hands = List(playerCount) { mutableListOf<String>() }
        var next = 0
        repeat(cardsPerPlayer) {
            for (player in 0 until playerCount) {
                hands[player] += cards[next++]
            }
        }
        return DealtCards(hands = hands.map { it.toList() }, deck = cards.drop(next))
    }
}
