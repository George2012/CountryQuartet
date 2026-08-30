package com.countryquartet.game.ai

import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.game.PlayerSeat
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.GameState
import kotlin.random.Random

/** What one simulated game produced. */
data class SimulationResult(
    val seed: Long,
    val moves: Int,
    val finalState: GameState,
)

/**
 * Plays complete AI-vs-AI-vs-AI-vs-AI games and checks the table after every
 * single move, so a lost or duplicated card is caught at the move that caused
 * it rather than at the end of the game.
 */
class GameSimulator(
    private val engine: GameEngine,
    private val gameData: GameData,
) {

    private val deck: List<String> = gameData.countryIds

    fun play(seed: Long): SimulationResult {
        val random = Random(seed)
        val strategy = BasicAi(random)
        var state = engine.newGame(AI_ONLY_SEATS, random)
        checkTable(state, seed, move = 0)

        var moves = 0
        while (!state.isFinished) {
            check(moves < MOVE_LIMIT) { "seed $seed did not finish within $MOVE_LIMIT moves" }
            val request = strategy.chooseRequest(engine, state)
            check(engine.isLegalRequest(state, request.targetPlayerId, request.countryId)) {
                "seed $seed move $moves: AI proposed the illegal request $request"
            }
            state = engine.ask(state, request.targetPlayerId, request.countryId).state
            moves++
            checkTable(state, seed, moves)
        }
        return SimulationResult(seed, moves, state)
    }

    /**
     * Every card is somewhere - a hand, a completed quartet or the draw pile -
     * exactly once, and no score appeared from nowhere.
     */
    private fun checkTable(state: GameState, seed: Long, move: Int) {
        val where = "seed $seed move $move"
        val inHands = state.players.flatMap { it.cards }
        val locked = state.players.flatMap { player ->
            player.completedQuartets.flatMap { gameData.quartet(it).countryIds }
        }
        // The draw pile holds cards too, so it counts towards the deck total.
        val all = inHands + locked + state.deck

        check(all.size == deck.size) { "$where: expected ${deck.size} cards but found ${all.size}" }
        check(all.distinct().size == all.size) {
            "$where: duplicated cards ${all.groupingBy { it }.eachCount().filterValues { it > 1 }.keys}"
        }
        check(all.toSet() == deck.toSet()) { "$where: the deck changed" }
        check(state.completedQuartetsCount == state.players.sumOf { it.score }) {
            "$where: score does not match completed quartets"
        }
        check(state.completedQuartetsCount <= TOTAL_QUARTETS) {
            "$where: ${state.completedQuartetsCount} quartets completed"
        }
        if (!state.isFinished) {
            check(state.currentPlayer.cards.isNotEmpty()) { "$where: player to move has no cards" }
        }
    }

    companion object {
        const val MOVE_LIMIT = 10_000
        const val TOTAL_QUARTETS = 13

        /** Four computer players, as required for the simulation. */
        val AI_ONLY_SEATS = listOf(
            PlayerSeat("ai_0", "AI 0", isHuman = false),
            PlayerSeat("ai_1", "AI 1", isHuman = false),
            PlayerSeat("ai_2", "AI 2", isHuman = false),
            PlayerSeat("ai_3", "AI 3", isHuman = false),
        )
    }
}
