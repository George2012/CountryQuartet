package com.countryquartet.game.viewmodel

import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Quartet

/** The human player's current picks: quartet, missing country and opponent. */
data class Selection(
    val quartetId: String? = null,
    val countryId: String? = null,
    val opponentId: String? = null,
    /**
     * (opponentId, quartetId) pairs the current player has already confirmed
     * hold at least one card, so asking for a specific country from that pair
     * does not need to ask about the region again first. Cleared whenever the
     * turn passes to someone else.
     */
    val confirmedRegions: Set<Pair<String, String>> = emptySet(),
)

/** One quartet the human holds cards of, split into owned and still missing. */
data class QuartetGroup(
    val quartet: Quartet,
    val owned: List<Country>,
    val missing: List<Country>,
)

/** A row of the score board. */
data class PlayerStanding(
    val id: String,
    val name: String,
    val isHuman: Boolean,
    val score: Int,
    val cardCount: Int,
    val isCurrent: Boolean,
    val isWinner: Boolean,
)

/**
 * What just happened, as data rather than text.
 *
 * The screen turns these into localizable strings, so no user-facing wording
 * lives in the ViewModel. The "is human" flags let it pick a sentence that
 * reads correctly for the player being addressed ("You do not have Lebanon"
 * rather than "You does not have Lebanon").
 */
sealed interface GameMessage {

    data class CardReceived(
        val askerName: String,
        val targetName: String,
        val countryName: String,
        val askerIsHuman: Boolean,
        val targetIsHuman: Boolean,
    ) : GameMessage

    data class CardRefused(
        val askerName: String,
        val targetName: String,
        val countryName: String,
        val askerIsHuman: Boolean,
        val targetIsHuman: Boolean,
    ) : GameMessage

    /** The asked player confirmed they hold at least one card of the region. */
    data class RegionPresent(
        val askerName: String,
        val targetName: String,
        val quartetName: String,
        val askerIsHuman: Boolean,
        val targetIsHuman: Boolean,
    ) : GameMessage

    /** The asked player holds none of the region, so the turn is over. */
    data class RegionAbsent(
        val askerName: String,
        val targetName: String,
        val quartetName: String,
        val askerIsHuman: Boolean,
        val targetIsHuman: Boolean,
    ) : GameMessage

    data class QuartetCompleted(
        val playerName: String,
        val targetName: String,
        val countryName: String,
        val askerIsHuman: Boolean,
        val targetIsHuman: Boolean,
        val quartet: Quartet,
        val countries: List<Country>,
    ) : GameMessage
}

/** Everything the game screen needs to draw itself. */
sealed interface GameUiState {

    /** The content is still being read from the assets. */
    data object Loading : GameUiState

    /** The content could not be loaded; the screen shows the reason. */
    data class Failed(val message: String) : GameUiState

    data class Playing(
        val hand: List<QuartetGroup>,
        val humanCompletedQuartets: List<QuartetEntry>,
        val standings: List<PlayerStanding>,
        val completedQuartetsCount: Int,
        val totalQuartets: Int,
        /** Cards left in the draw pile, shown on the deck. */
        val deckCount: Int,
        val currentPlayerName: String,
        val isHumanTurn: Boolean,
        val selection: Selection,
        /** Whether the region step can be asked: a quartet and opponent are picked, but not yet confirmed. */
        val canAskRegion: Boolean,
        /** Whether the specific card can be asked: the region is confirmed and a country is picked. */
        val canAsk: Boolean,
        val message: GameMessage?,
        /** Every action taken so far this game, most recent first. */
        val history: List<GameMessage>,
        val animationsEnabled: Boolean,
        /** Whether the computer players move on their own instead of on Next. */
        val autoPlay: Boolean,
        /**
         * Whether the human's lost turn still owes them a card. The hand and
         * the deck shown are the ones from before the failed ask until they
         * take it.
         */
        val mustTakeCard: Boolean,
        val isFinished: Boolean,
        val winnerNames: List<String>,
    ) : GameUiState {

        /**
         * Whether a computer ask is waiting to be played. True for exactly as
         * long as the Next button has something to do.
         */
        val canStep: Boolean get() = !isFinished && !isHumanTurn && !mustTakeCard

        /** Whether the human can pick and ask right now. */
        val canAct: Boolean get() = isHumanTurn && !mustTakeCard

        /** Whether the opponent currently picked has confirmed holding a card of [quartetId] this turn. */
        fun isRegionConfirmed(quartetId: String): Boolean {
            val opponentId = selection.opponentId ?: return false
            return (opponentId to quartetId) in selection.confirmedRegions
        }

        /** The three opponents, in seating order. */
        val opponents: List<PlayerStanding> get() = standings.filterNot { it.isHuman }

        val human: PlayerStanding get() = standings.first { it.isHuman }

        /** The quartet the human is currently working on, if any. */
        val selectedGroup: QuartetGroup?
            get() = hand.firstOrNull { it.quartet.id == selection.quartetId }

        /** The quartet just completed, while its banner should be on screen. */
        val justCompletedQuartet: GameMessage.QuartetCompleted?
            get() = message as? GameMessage.QuartetCompleted

        /** True when more than one player shares the top score. */
        val isDraw: Boolean get() = isFinished && winnerNames.size > 1
    }
}
