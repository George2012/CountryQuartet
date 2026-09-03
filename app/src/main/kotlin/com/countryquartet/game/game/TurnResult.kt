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

    /**
     * The opponent did not have the card, so the turn moves on.
     *
     * [drewCountryId] is the card the lost turn paid out, or null once the
     * draw pile has run out.
     */
    data class Failure(
        override val askingPlayerId: String,
        override val targetPlayerId: String,
        override val countryId: String,
        val drewCountryId: String? = null,
    ) : RequestOutcome {

        /** Whether the pile still had a card to pay the lost turn with. */
        val drewFromDeck: Boolean get() = drewCountryId != null
    }
}

/** The state after a move together with what the move did, for UI feedback. */
data class TurnResult(
    val state: GameState,
    val outcome: RequestOutcome,
)

/**
 * What happened when a player asked an opponent whether they hold any card of
 * a whole quartet, the step that now comes before naming a specific country.
 */
sealed interface RegionOutcome {

    val askingPlayerId: String
    val targetPlayerId: String
    val quartetId: String

    /**
     * The opponent holds at least one card of the quartet. The turn continues
     * and the asking player may now name a specific country to request.
     */
    data class Present(
        override val askingPlayerId: String,
        override val targetPlayerId: String,
        override val quartetId: String,
    ) : RegionOutcome

    /**
     * The opponent holds none of the quartet, so the turn moves on - exactly
     * like a failed card request.
     *
     * [drewCountryId] is the card the lost turn paid out, or null once the
     * draw pile has run out.
     */
    data class Absent(
        override val askingPlayerId: String,
        override val targetPlayerId: String,
        override val quartetId: String,
        val drewCountryId: String? = null,
    ) : RegionOutcome {

        /** Whether the pile still had a card to pay the lost turn with. */
        val drewFromDeck: Boolean get() = drewCountryId != null
    }
}

/** The state after a region check together with what it revealed. */
data class RegionResult(
    val state: GameState,
    val outcome: RegionOutcome,
)
