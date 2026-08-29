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
class GameEngine(private val gameData: GameData) {

    /** Starts a game: shuffle, deal and lay down quartets that were dealt complete. */
    fun newGame(
        seats: List<PlayerSeat> = defaultSeats(),
        random: Random = Random.Default,
    ): GameState {
        require(seats.size == PLAYER_COUNT) {
            "A game needs exactly $PLAYER_COUNT players but got ${seats.size}"
        }
        require(seats.map { it.id }.distinct().size == seats.size) { "Player ids must be unique" }

        val hands = Dealer.deal(Deck.shuffled(gameData, random), seats.size)
        val players = seats.mapIndexed { index, seat ->
            Player(id = seat.id, name = seat.name, isHuman = seat.isHuman, cards = hands[index])
        }
        val dealt = GameState(
            players = players,
            currentPlayerIndex = 0,
            status = GameStatus.IN_PROGRESS,
            winnerIds = emptyList(),
        )
        // A hand of 13 can already hold complete quartets. They have to be laid
        // down now: their owner could never ask for cards they already hold, so
        // the quartet would stay open forever and the game could never end.
        val resolved = players.indices.fold(dealt) { state, index ->
            state.withPlayer(index, completeQuartets(state.players[index]).first)
        }
        return settle(resolved)
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
            return TurnResult(
                state = settle(advanceTurn(state)),
                outcome = RequestOutcome.Failure(asking.id, target.id, countryId),
            )
        }

        val (receiver, completed) = completeQuartets(asking.copy(cards = asking.cards + countryId))
        val afterTransfer = state
            .withPlayer(targetIndex, target.copy(cards = target.cards - countryId))
            .withPlayer(askingIndex, receiver)

        return TurnResult(
            // A successful request keeps the turn, so the state is only settled,
            // never advanced.
            state = settle(afterTransfer),
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

    /**
     * Finishes the game once every quartet is collected, and otherwise makes
     * sure the player to move actually holds cards.
     */
    private fun settle(state: GameState): GameState {
        if (state.completedQuartetsCount == gameData.quartets.size) {
            val best = state.players.maxOf { it.score }
            return state.copy(
                status = GameStatus.FINISHED,
                winnerIds = state.players.filter { it.score == best }.map { it.id },
            )
        }
        // A player who ran out of cards has nothing legal to ask for, so the
        // turn passes on. The written rules do not cover empty hands; without
        // this the game would deadlock.
        return if (state.currentPlayer.cards.isEmpty()) advanceTurn(state) else state
    }

    private fun advanceTurn(state: GameState): GameState {
        val size = state.players.size
        for (step in 1..size) {
            val index = (state.currentPlayerIndex + step) % size
            if (state.players[index].cards.isNotEmpty()) return state.copy(currentPlayerIndex = index)
        }
        return state
    }

    private fun GameState.withPlayer(index: Int, player: Player): GameState =
        copy(players = players.toMutableList().also { it[index] = player })

    companion object {
        const val PLAYER_COUNT = 4
        const val CARDS_PER_PLAYER = 13
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
