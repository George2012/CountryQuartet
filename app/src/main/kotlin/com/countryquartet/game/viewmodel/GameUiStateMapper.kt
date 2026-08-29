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
    val canAsk = isHumanTurn &&
        selection.countryId != null &&
        selection.opponentId != null &&
        engine.isLegalRequest(game, selection.opponentId, selection.countryId)

    return GameUiState.Playing(
        hand = hand,
        humanCompletedQuartets = human.completedQuartets.map(gameData::quartet),
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
        currentPlayerName = game.currentPlayer.name,
        isHumanTurn = isHumanTurn,
        selection = selection,
        canAsk = canAsk,
        message = message,
        isFinished = game.isFinished,
        winnerNames = game.winnerIds.map { game.player(it).name },
    )
}
