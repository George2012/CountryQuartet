package com.countryquartet.game.viewmodel

import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Quartet

/** The human player's current picks: quartet, missing country and opponent. */
data class Selection(
    val quartetId: String? = null,
    val countryId: String? = null,
    val opponentId: String? = null,
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
 * lives in the ViewModel.
 */
sealed interface GameMessage {

    data class CardReceived(
        val askerName: String,
        val targetName: String,
        val countryName: String,
    ) : GameMessage

    data class CardRefused(
        val askerName: String,
        val targetName: String,
        val countryName: String,
    ) : GameMessage

    data class QuartetCompleted(
        val playerName: String,
        val quartetName: String,
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
        val humanCompletedQuartets: List<Quartet>,
        val standings: List<PlayerStanding>,
        val completedQuartetsCount: Int,
        val totalQuartets: Int,
        val currentPlayerName: String,
        val isHumanTurn: Boolean,
        val selection: Selection,
        val canAsk: Boolean,
        val message: GameMessage?,
        val isFinished: Boolean,
        val winnerNames: List<String>,
    ) : GameUiState {

        /** The three opponents, in seating order. */
        val opponents: List<PlayerStanding> get() = standings.filterNot { it.isHuman }

        val human: PlayerStanding get() = standings.first { it.isHuman }

        /** The quartet the human is currently working on, if any. */
        val selectedGroup: QuartetGroup?
            get() = hand.firstOrNull { it.quartet.id == selection.quartetId }

        /** True when more than one player shares the top score. */
        val isDraw: Boolean get() = isFinished && winnerNames.size > 1
    }
}
