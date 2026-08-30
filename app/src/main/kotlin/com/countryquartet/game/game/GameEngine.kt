package com.countryquartet.game.game

import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.GameState
import com.countryquartet.game.model.GameStatus
import com.countryquartet.game.model.Player
import kotlin.random.Random

/**
 * The rules of Country Quartet.
 *
 * Pure Kotlin: no Android, no Compose, no global state. Every move returns a
 * new [GameState], so the same engine serves the human player, the AI and the
 * tests.
 */
class GameEngine(
    private val gameData: GameData,
    /** Opening hand size. One place to change how long a game runs. */
    private val cardsPerPlayer: Int = DEFAULT_CARDS_PER_PLAYER,
) {

    /** Starts a game: shuffle, deal and lay down quartets that were dealt complete. */
    fun newGame(
        seats: List<PlayerSeat> = defaultSeats(),
        random: Random = Random.Default,
    ): GameState {
        require(seats.size == PLAYER_COUNT) {
            "A game needs exactly $PLAYER_COUNT players but got ${seats.size}"
        }
        require(seats.map { it.id }.distinct().size == seats.size) { "Player ids must be unique" }

        val dealtCards = Dealer.deal(Deck.shuffled(gameData, random), seats.size, cardsPerPlayer)
        val players = seats.mapIndexed { index, seat ->
            Player(
                id = seat.id,
                name = seat.name,
                isHuman = seat.isHuman,
                cards = dealtCards.hands[index],
            )
        }
        val dealt = GameState(
            players = players,
            currentPlayerIndex = 0,
            status = GameStatus.IN_PROGRESS,
            winnerIds = emptyList(),
            deck = dealtCards.deck,
        )
        // A hand of 13 can already hold complete quartets. They have to be laid
        // down now: their owner could never ask for cards they already hold, so
        // the quartet would stay open forever and the game could never end.
        val resolved = players.indices.fold(dealt) { state, index ->
            state.withPlayer(index, completeQuartets(state.players[index]).first)
        }
        return beginTurn(settle(resolved))
    }

    /**
     * The current player asks [targetPlayerId] for [countryId].
     *
     * @throws IllegalMoveException if the request breaks a rule.
     */
    fun ask(state: GameState, targetPlayerId: String, countryId: String): TurnResult {
        requireLegal(state, targetPlayerId, countryId)

        val askingIndex = state.currentPlayerIndex
        val asking = state.players[askingIndex]
        val targetIndex = state.players.indexOfFirst { it.id == targetPlayerId }
        val target = state.players[targetIndex]

        if (countryId !in target.cards) {
            // Losing the turn is paid for with one card from the draw pile.
            val afterDraw = drawCard(state, askingIndex)
            return TurnResult(
                state = beginTurn(settle(advanceTurn(afterDraw))),
                outcome = RequestOutcome.Failure(
                    askingPlayerId = asking.id,
                    targetPlayerId = target.id,
                    countryId = countryId,
                    drewFromDeck = afterDraw.deckCount < state.deckCount,
                ),
            )
        }

        val (receiver, completed) = completeQuartets(asking.copy(cards = asking.cards + countryId))
        val afterTransfer = state
            .withPlayer(targetIndex, target.copy(cards = target.cards - countryId))
            .withPlayer(askingIndex, receiver)

        return TurnResult(
            // A successful request keeps the turn, so the state is never
            // advanced; beginTurn only steps in if the hand is now empty.
            state = beginTurn(settle(afterTransfer)),
            outcome = RequestOutcome.Success(
                askingPlayerId = asking.id,
                targetPlayerId = target.id,
                countryId = countryId,
                completedQuartetId = completed.firstOrNull(),
            ),
        )
    }

    /** Every request the current player is allowed to make right now. */
    fun legalRequests(state: GameState): List<CardRequest> {
        if (state.isFinished) return emptyList()
        val asking = state.currentPlayer
        val wanted = representedQuartets(asking).flatMap { quartetId ->
            missingCountries(asking, quartetId)
        }
        return state.players
            .filter { it.id != asking.id }
            .flatMap { opponent -> wanted.map { CardRequest(opponent.id, it) } }
    }

    /** Whether the current player may ask [targetPlayerId] for [countryId]. */
    fun isLegalRequest(state: GameState, targetPlayerId: String, countryId: String): Boolean =
        illegalReason(state, targetPlayerId, countryId) == null

    /** The quartets [player] holds at least one card of. */
    fun representedQuartets(player: Player): List<String> =
        player.cards.map { gameData.country(it).quartetId }.distinct()

    /** The countries of [quartetId] that [player] still needs. */
    fun missingCountries(player: Player, quartetId: String): List<String> =
        gameData.quartet(quartetId).countryIds.filterNot { it in player.cards }

    /**
     * How many cards of each represented quartet [player] holds, keyed by
     * quartet id. Used by the AI to pick its strongest quartet and by the UI to
     * show collection progress.
     */
    fun quartetProgress(player: Player): Map<String, Int> =
        player.cards.groupingBy { gameData.country(it).quartetId }.eachCount()

    private fun requireLegal(state: GameState, targetPlayerId: String, countryId: String) {
        illegalReason(state, targetPlayerId, countryId)?.let { throw IllegalMoveException(it) }
    }

    private fun illegalReason(state: GameState, targetPlayerId: String, countryId: String): String? {
        if (state.isFinished) return "The game is already finished"
        val country = gameData.countryOrNull(countryId) ?: return "Unknown country: $countryId"
        val asking = state.currentPlayer
        if (targetPlayerId == asking.id) return "A player cannot ask themselves"
        state.playerOrNull(targetPlayerId) ?: return "Unknown player: $targetPlayerId"
        if (countryId in asking.cards) return "${asking.name} already owns $countryId"
        if (state.players.any { country.quartetId in it.completedQuartets }) {
            return "Quartet ${country.quartetId} is already completed"
        }
        if (country.quartetId !in representedQuartets(asking)) {
            return "${asking.name} holds no card of quartet ${country.quartetId}"
        }
        return null
    }

    /** Moves every quartet the player now holds in full out of their hand. */
    private fun completeQuartets(player: Player): Pair<Player, List<String>> {
        var current = player
        val completed = mutableListOf<String>()
        while (true) {
            val quartetId = representedQuartets(current).firstOrNull { candidate ->
                gameData.quartet(candidate).countryIds.all { it in current.cards }
            } ?: break
            val countryIds = gameData.quartet(quartetId).countryIds.toSet()
            current = current.copy(
                cards = current.cards - countryIds,
                completedQuartets = current.completedQuartets + quartetId,
            )
            completed += quartetId
        }
        return current to completed
    }

    /** Takes the top card of the draw pile, if there is one. */
    private fun drawCard(state: GameState, playerIndex: Int): GameState {
        val card = state.deck.firstOrNull() ?: return state
        val receiver = state.players[playerIndex].copy(
            cards = state.players[playerIndex].cards + card,
        )
        // A drawn card can complete a quartet just like a card that was asked for.
        return state.copy(deck = state.deck.drop(1))
            .withPlayer(playerIndex, completeQuartets(receiver).first)
    }

    /**
     * Hands the turn to a player who can actually use it.
     *
     * A player who starts a turn with no cards takes one from the draw pile and
     * loses the turn anyway. Once the pile is empty they are simply passed over,
     * and play carries on between whoever still holds cards.
     */
    private fun beginTurn(state: GameState): GameState {
        var current = state
        // Each pass either removes a card from the pile or moves along the
        // table, so this cannot run away.
        val limit = state.deck.size + state.players.size + 1
        repeat(limit) {
            if (current.isFinished || current.currentPlayer.cards.isNotEmpty()) return current
            current = when {
                current.deck.isNotEmpty() -> advanceTurn(drawCard(current, current.currentPlayerIndex))
                current.players.any { it.cards.isNotEmpty() } -> advanceTurn(current)
                // Nobody holds a card and the pile is gone: the game is over.
                else -> return finish(current)
            }
            current = settle(current)
        }
        return current
    }

    /** Finishes the game once every quartet has been collected. */
    private fun settle(state: GameState): GameState =
        if (state.completedQuartetsCount == gameData.quartets.size) finish(state) else state

    private fun finish(state: GameState): GameState {
        val best = state.players.maxOf { it.score }
        return state.copy(
            status = GameStatus.FINISHED,
            winnerIds = state.players.filter { it.score == best }.map { it.id },
        )
    }

    /** Simply the next seat: skipping empty hands is [beginTurn]'s job now. */
    private fun advanceTurn(state: GameState): GameState =
        state.copy(currentPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size)

    private fun GameState.withPlayer(index: Int, player: Player): GameState =
        copy(players = players.toMutableList().also { it[index] = player })

    companion object {
        const val PLAYER_COUNT = 4

        /**
         * Cards dealt to each player at the start. The rest become the draw
         * pile, so a smaller hand means a longer pile and a longer game.
         */
        const val DEFAULT_CARDS_PER_PLAYER = 5

        const val HUMAN_ID = "human"

        /** Default table: the human plus three AI opponents. The UI may pass its own names. */
        fun defaultSeats(): List<PlayerSeat> = listOf(
            PlayerSeat(HUMAN_ID, "You", isHuman = true),
            PlayerSeat("ai_1", "AI 1", isHuman = false),
            PlayerSeat("ai_2", "AI 2", isHuman = false),
            PlayerSeat("ai_3", "AI 3", isHuman = false),
        )
    }
}
