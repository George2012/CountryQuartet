package com.countryquartet.game.game

import com.countryquartet.game.data.AssetFiles
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.GameState
import com.countryquartet.game.model.GameStatus
import com.countryquartet.game.model.Player
import com.countryquartet.game.repository.CountryRepository

/**
 * Shared fixtures. The engine tests run against the real dataset, so the
 * quartets used below are the ones that ship with the game.
 */
object TestGame {

    val gameData: GameData = CountryRepository(AssetFiles).gameData()

    val engine = GameEngine(gameData)

    /** Ids of the four players in every fixture state. */
    val PLAYER_IDS = listOf("p0", "p1", "p2", "p3")

    /**
     * Builds a state directly from the given hands, bypassing the deal so each
     * test can describe exactly the situation it cares about.
     */
    fun stateOf(
        vararg hands: List<String>,
        currentPlayerIndex: Int = 0,
        completed: List<List<String>> = List(hands.size) { emptyList() },
        deck: List<String> = emptyList(),
    ): GameState = GameState(
        players = hands.mapIndexed { index, cards ->
            Player(
                id = PLAYER_IDS[index],
                name = "Player $index",
                isHuman = index == 0,
                cards = cards,
                completedQuartets = completed[index],
            )
        },
        currentPlayerIndex = currentPlayerIndex,
        status = GameStatus.IN_PROGRESS,
        winnerIds = emptyList(),
        deck = deck,
    )

    /** Every card: in a hand, in a completed quartet, or still in the draw pile. */
    fun allCards(state: GameState): List<String> = state.players.flatMap { player ->
        player.cards + player.completedQuartets.flatMap { gameData.quartet(it).countryIds }
    } + state.deck

    /** Countries of [quartetId] in dataset order. */
    fun cardsOf(quartetId: String): List<String> = gameData.quartet(quartetId).countryIds
}
