package com.countryquartet.game.viewmodel

import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.GameState

/**
 * Turns an engine [GameState] into something the screen can render.
 *
 * Pure and free of coroutines, so the whole screen-facing shape of the game is
 * covered by plain JVM tests.
 */
internal fun playingState(
    engine: GameEngine,
    gameData: GameData,
    game: GameState,
    selection: Selection,
    message: GameMessage?,
    history: List<GameMessage> = emptyList(),
    animationsEnabled: Boolean = true,
): GameUiState.Playing {
    val human = game.players.first { it.isHuman }
    val ownedIds = human.cards.toSet()

    val hand = engine.representedQuartets(human)
        .map { quartetId ->
            val quartet = gameData.quartet(quartetId)
            val countries = quartet.countryIds.map(gameData::country)
            QuartetGroup(
                quartet = quartet,
                owned = countries.filter { it.id in ownedIds },
                missing = countries.filterNot { it.id in ownedIds },
            )
        }
        // Show the quartets closest to completion first.
        .sortedWith(compareByDescending<QuartetGroup> { it.owned.size }.thenBy { it.quartet.name })

    val isHumanTurn = !game.isFinished && game.currentPlayer.isHuman
    // Whether the (opponent, quartet) pair currently picked has already been
    // confirmed present this turn - derived from the country when one is
    // picked, so it stays correct even if selection.quartetId lags behind.
    val selectedQuartetId = selection.countryId?.let { gameData.country(it).quartetId } ?: selection.quartetId
    val regionConfirmed = selection.opponentId != null &&
        selectedQuartetId != null &&
        (selection.opponentId to selectedQuartetId) in selection.confirmedRegions
    val canAskRegion = isHumanTurn &&
        selection.quartetId != null &&
        selection.opponentId != null &&
        !regionConfirmed &&
        engine.isLegalRegionRequest(game, selection.opponentId, selection.quartetId)
    val canAsk = isHumanTurn &&
        selection.countryId != null &&
        selection.opponentId != null &&
        regionConfirmed &&
        engine.isLegalRequest(game, selection.opponentId, selection.countryId)

    return GameUiState.Playing(
        hand = hand,
        humanCompletedQuartets = human.completedQuartets.map { quartetId ->
            QuartetEntry(
                quartet = gameData.quartet(quartetId),
                countries = gameData.countriesOf(quartetId),
            )
        },
        standings = game.players.mapIndexed { index, player ->
            PlayerStanding(
                id = player.id,
                name = player.name,
                isHuman = player.isHuman,
                score = player.score,
                cardCount = player.cards.size,
                isCurrent = !game.isFinished && index == game.currentPlayerIndex,
                isWinner = player.id in game.winnerIds,
            )
        },
        completedQuartetsCount = game.completedQuartetsCount,
        totalQuartets = gameData.quartets.size,
        deckCount = game.deckCount,
        currentPlayerName = game.currentPlayer.name,
        isHumanTurn = isHumanTurn,
        selection = selection,
        canAskRegion = canAskRegion,
        canAsk = canAsk,
        message = message,
        history = history,
        animationsEnabled = animationsEnabled,
        isFinished = game.isFinished,
        winnerNames = game.winnerIds.map { game.player(it).name },
    )
}
