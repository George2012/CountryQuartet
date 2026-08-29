package com.countryquartet.game.game

import com.countryquartet.game.model.GameState
import kotlin.random.Random

/**
 * Test driver that plays a whole game by picking random legal requests.
 *
 * This is not the game AI (that arrives in Phase 4) - it only exists so the
 * end-game rules can be exercised on real, fully played games.
 */
object GameRunner {

    /** Safety net: a healthy game ends in far fewer moves than this. */
    const val MOVE_LIMIT = 20_000

    fun playToEnd(engine: GameEngine, start: GameState, random: Random = Random(0)): GameState {
        var state = start
        var moves = 0
        while (!state.isFinished) {
            check(moves++ < MOVE_LIMIT) { "Game did not finish within $MOVE_LIMIT moves" }
            val requests = engine.legalRequests(state)
            check(requests.isNotEmpty()) { "No legal request available while the game is running" }
            val request = requests[random.nextInt(requests.size)]
            state = engine.ask(state, request.targetPlayerId, request.countryId).state
        }
        return state
    }
}
