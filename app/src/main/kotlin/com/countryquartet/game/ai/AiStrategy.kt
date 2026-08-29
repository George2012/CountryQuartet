package com.countryquartet.game.ai

import com.countryquartet.game.game.CardRequest
import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.model.GameState

/**
 * Decides what a computer player asks for on its turn.
 *
 * A strategy only *chooses* a move; the move itself is always carried out by
 * [GameEngine], so the rules live in exactly one place. Smarter opponents can
 * be added later as further implementations without touching the engine.
 */
fun interface AiStrategy {

    /**
     * The request for `state.currentPlayer`.
     *
     * The returned request is expected to be legal; callers hand it straight to
     * [GameEngine.ask].
     */
    fun chooseRequest(engine: GameEngine, state: GameState): CardRequest
}
