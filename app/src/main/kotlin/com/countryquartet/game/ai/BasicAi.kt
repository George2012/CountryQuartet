package com.countryquartet.game.ai

import com.countryquartet.game.game.CardRequest
import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.model.GameState
import kotlin.random.Random

/**
 * The opponent shipped with the first release.
 *
 * It plays the straightforward line: work on the quartet it has collected most
 * of, ask for one of the countries still missing there, and pick an opponent at
 * random. It keeps no memory of earlier turns - remembering who denied which
 * card is deliberately left to a later, smarter strategy.
 *
 * Every candidate comes from [GameEngine], so this class contains no rules of
 * its own.
 */
class BasicAi(private val random: Random = Random.Default) : AiStrategy {

    override fun chooseRequest(engine: GameEngine, state: GameState): CardRequest {
        val me = state.currentPlayer
        val progress = engine.quartetProgress(me)
        require(progress.isNotEmpty()) { "${me.name} has no cards to play with" }

        // 1. Prefer the quartet the player already owns the most cards of.
        val mostCards = progress.values.max()
        val quartetId = progress.filterValues { it == mostCards }.keys.random(random)

        // 2. Ask for one of the countries still missing from it.
        val countryId = engine.missingCountries(me, quartetId).random(random)

        // 3. Pick an opponent. Players who are out of cards can only ever say
        //    no, so they are skipped while anyone else is still holding cards.
        val opponents = state.players.filter { it.id != me.id }
        val target = opponents.filter { it.cards.isNotEmpty() }.ifEmpty { opponents }.random(random)

        return CardRequest(targetPlayerId = target.id, countryId = countryId)
    }
}
