package com.countryquartet.game.game

import com.countryquartet.game.model.GameState

/** A seat at the table, used to set up a new game. */
data class PlayerSeat(
    val id: String,
    val name: String,
    val isHuman: Boolean,
)

/** One legal "give me this country" move. */
data class CardRequest(
    val targetPlayerId: String,
    val countryId: String,
)

/** What happened when a player asked an opponent for a card. */
sealed interface RequestOutcome {

    val askingPlayerId: String
    val targetPlayerId: String
    val countryId: String

    /**
     * The opponent handed the card over. The asking player keeps the turn.
     *
     * [completedQuartetId] is set when this card finished a quartet.
     */
    data class Success(
        override val askingPlayerId: String,
        override val targetPlayerId: String,
        override val countryId: String,
        val completedQuartetId: String? = null,
    ) : RequestOutcome

    /** The opponent did not have the card, so the turn moves on. */
    data class Failure(
        override val askingPlayerId: String,
        override val targetPlayerId: String,
        override val countryId: String,
    ) : RequestOutcome
}

/** The state after a move together with what the move did, for UI feedback. */
data class TurnResult(
    val state: GameState,
    val outcome: RequestOutcome,
)
