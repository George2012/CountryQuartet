package com.countryquartet.game.model

/**
 * One participant of a game.
 *
 * [cards] holds the country ids still in the player's hand; the four cards of a
 * finished quartet leave the hand and only the quartet id remains in
 * [completedQuartets].
 */
data class Player(
    val id: String,
    val name: String,
    val isHuman: Boolean,
    val cards: List<String> = emptyList(),
    val completedQuartets: List<String> = emptyList(),
) {
    /** One point per completed quartet. */
    val score: Int get() = completedQuartets.size
}
