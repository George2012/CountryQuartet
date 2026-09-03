package com.countryquartet.game.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.countryquartet.game.AppGraph
import com.countryquartet.game.ai.AiStrategy
import com.countryquartet.game.ai.BasicAi
import com.countryquartet.game.data.AssetGameDataSource
import com.countryquartet.game.game.CardRequest
import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.game.PlayerSeat
import com.countryquartet.game.game.RegionOutcome
import com.countryquartet.game.game.RequestOutcome
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.GameState
import com.countryquartet.game.repository.CountryRepository
import com.countryquartet.game.repository.InMemorySettingsRepository
import com.countryquartet.game.repository.InMemoryStatisticsRepository
import com.countryquartet.game.repository.SettingsRepository
import com.countryquartet.game.repository.StatisticsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Drives one game: holds the engine state, applies the human player's moves and
 * plays the three computer turns.
 *
 * All rules stay in [GameEngine]; this class only decides *when* a move is made
 * and how the result is presented.
 */
class GameViewModel(
    private val repository: CountryRepository,
    private val ai: AiStrategy = BasicAi(),
    /** Seedable so tests can replay an exact deal. */
    private val random: Random = Random.Default,
    private val settings: SettingsRepository = InMemorySettingsRepository.shared,
    private val statistics: StatisticsRepository = InMemoryStatisticsRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var gameData: GameData
    private lateinit var engine: GameEngine

    private var game: GameState? = null
    private var selection = Selection()
    private var message: GameMessage? = null
    /** Every message shown so far this game, most recent first. */
    private var history: List<GameMessage> = emptyList()
    private var aiTurns: Job? = null

    /**
     * Whether the computer players move on their own.
     *
     * Off by default: a game is stepped through one ask at a time so a player
     * can follow what the opponents are doing. Turning it on hands the pacing
     * back to the timer.
     */
    private var autoPlay = false

    /**
     * The ask the current computer player is in the middle of. It is chosen
     * once and then survives between steps, because naming the region and
     * naming the country are two separate presses of Next.
     */
    private var pendingRequest: CardRequest? = null

    /**
     * (opponent, quartet) pairs the current computer player has already had
     * confirmed this turn, so it does not ask about the same region twice.
     */
    private val confirmedThisTurn = mutableSetOf<Pair<String, String>>()

    /** Who the above two belong to; anyone else starts with a clean slate. */
    private var lastAsker: String? = null

    /**
     * The picture to keep showing while the human still owes themselves the
     * card a lost turn pays out: the state as it stood *before* the failed ask.
     *
     * The engine settles the draw as part of the failed request, so this is
     * only how it is presented - the card stays on the pile on screen, and the
     * turn stays with the human, until they take it. Never set while auto play
     * is on, which does not stop for anything.
     */
    private var pendingTake: GameState? = null

    /** The card [pendingTake] owes, so taking it can say which one it was. */
    private var pendingTakeCard: String? = null

    /** Guards against counting the same finished game more than once. */
    private var finishRecorded = false

    init {
        startNewGame()
        // A change on the settings screen has to reach a game that is already
        // running, if only to stop animating mid turn.
        viewModelScope.launch {
            settings.settings.collect { publish() }
        }
    }

    /** Deals a fresh game. Used on entry and by "Play again". */
    fun startNewGame() {
        aiTurns?.cancel()
        selection = Selection()
        message = null
        history = emptyList()
        finishRecorded = false
        // Every game starts stepped: auto play is a choice made per game.
        autoPlay = false
        pendingTake = null
        pendingTakeCard = null
        forgetAiTurn()
        try {
            if (!::gameData.isInitialized) {
                gameData = repository.gameData()
                engine = GameEngine(gameData)
            }
            game = engine.newGame(seats = drawSeats(), random = random)
            publish()
            continueWithAi()
        } catch (e: Exception) {
            // Broken or missing content must not take the app down.
            _uiState.value = GameUiState.Failed(e.message ?: e.toString())
        }
    }

    /** Picks the quartet the human wants to work on and clears the chosen country. */
    fun selectQuartet(quartetId: String) {
        if (!isHumanTurn()) return
        selection = selection.copy(
            quartetId = quartetId.takeIf { it != selection.quartetId },
            countryId = null,
        )
        publish()
    }

    /** Picks one of the countries still missing from the selected quartet. */
    fun selectCountry(countryId: String) {
        if (!isHumanTurn()) return
        selection = selection.copy(countryId = countryId.takeIf { it != selection.countryId })
        publish()
    }

    /** Picks the opponent to ask. */
    fun selectOpponent(playerId: String) {
        if (!isHumanTurn()) return
        selection = selection.copy(opponentId = playerId.takeIf { it != selection.opponentId })
        publish()
    }

    /**
     * Asks the selected opponent whether they hold any card of the selected
     * quartet at all. This has to happen, and come back "yes", before a
     * specific country can be asked for.
     */
    fun askRegion() {
        val current = game ?: return
        val quartetId = selection.quartetId ?: return
        val opponentId = selection.opponentId ?: return
        if (!isHumanTurn() || !engine.isLegalRegionRequest(current, opponentId, quartetId)) return

        val result = engine.askRegion(current, opponentId, quartetId)
        game = result.state
        record(describeRegion(current, result.outcome))
        selection = when (result.outcome) {
            is RegionOutcome.Present -> selection.copy(
                confirmedRegions = selection.confirmedRegions + (opponentId to quartetId),
            )
            is RegionOutcome.Absent -> Selection()
        }
        awaitTake((result.outcome as? RegionOutcome.Absent)?.drewCountryId, current)
        forgetAiTurn()
        publish()
        continueWithAi()
    }

    /** Asks the selected opponent for the selected country. */
    fun ask() {
        val current = game ?: return
        val countryId = selection.countryId ?: return
        val opponentId = selection.opponentId ?: return
        val quartetId = gameData.country(countryId).quartetId
        if (!isHumanTurn() ||
            (opponentId to quartetId) !in selection.confirmedRegions ||
            !engine.isLegalRequest(current, opponentId, countryId)
        ) return

        val result = engine.ask(current, opponentId, countryId)
        game = result.state
        record(describe(current, result.outcome))
        selection = when (result.outcome) {
            // The card is now owned, so the pick is cleared but the quartet
            // and the confirmed regions stay while the human keeps the turn.
            is RequestOutcome.Success -> selection.copy(
                quartetId = selection.quartetId.takeIf { id ->
                    result.state.players.first { it.isHuman }.cards.any { card ->
                        gameData.country(card).quartetId == id
                    }
                },
                countryId = null,
            )
            is RequestOutcome.Failure -> Selection()
        }
        awaitTake((result.outcome as? RequestOutcome.Failure)?.drewCountryId, current)
        forgetAiTurn()
        publish()
        continueWithAi()
    }

    /**
     * Plays the next computer ask: what the Next button does.
     *
     * Ignored while auto play is on, where the timer drives the same steps.
     */
    fun advance() {
        // A card still owed comes first: nothing moves until it is taken.
        if (autoPlay || pendingTake != null) return
        playOneAiStep()
    }

    /**
     * Takes the card a lost turn pays out: what the Take button under the deck
     * does. Only then does the hand grow, the pile shrink and the turn pass on.
     */
    fun takeCard() {
        if (pendingTake == null) return
        pendingTake = null
        // Named as it is handed over: a card that simply appeared in the hand
        // would leave the player guessing which one is new.
        pendingTakeCard?.let { record(GameMessage.CardTaken(gameData.country(it))) }
        pendingTakeCard = null
        publish()
        continueWithAi()
    }

    /**
     * Keeps [before] on screen while the human is owed [drewCountryId], so the
     * card is theirs only once they take it.
     */
    private fun awaitTake(drewCountryId: String?, before: GameState) {
        if (autoPlay || drewCountryId == null) return
        pendingTake = before
        pendingTakeCard = drewCountryId
    }

    /**
     * Turns auto play on or off.
     *
     * Switching it on picks the game up wherever the stepping left it, so a
     * half-played computer turn carries straight on.
     */
    fun setAutoPlay(enabled: Boolean) {
        if (autoPlay == enabled) return
        autoPlay = enabled
        if (enabled) {
            // Auto play does not stop for a card: anything owed is taken now.
            pendingTake = null
            pendingTakeCard = null
            continueWithAi()
        } else {
            aiTurns?.cancel()
        }
        publish()
    }

    private fun isHumanTurn(): Boolean {
        val current = game ?: return false
        return !current.isFinished && current.currentPlayer.isHuman
    }

    private fun isAiTurn(): Boolean {
        val current = game ?: return false
        return !current.isFinished && !current.currentPlayer.isHuman
    }

    /**
     * Plays computer turns on a timer until it is the human's turn again or
     * the game ends. Only used while auto play is on; otherwise the very same
     * steps wait for [advance].
     */
    private fun continueWithAi() {
        aiTurns?.cancel()
        if (!autoPlay) return
        aiTurns = viewModelScope.launch {
            while (isAiTurn()) {
                delay(aiThinkingDelay())
                playOneAiStep()
            }
        }
    }

    /**
     * Plays exactly one computer ask, in the same two steps a human takes: the
     * region question, and then - once that came back "yes" - the specific
     * country. A region already confirmed earlier in the same turn is not
     * asked about again, so that step is skipped.
     */
    private fun playOneAiStep() {
        val current = game ?: return
        if (!isAiTurn()) return

        if (current.currentPlayer.id != lastAsker) {
            forgetAiTurn()
            lastAsker = current.currentPlayer.id
        }

        val request = pendingRequest ?: ai.chooseRequest(engine, current).also { pendingRequest = it }
        val quartetId = gameData.country(request.countryId).quartetId
        val pair = request.targetPlayerId to quartetId

        if (pair in confirmedThisTurn) {
            val result = engine.ask(current, request.targetPlayerId, request.countryId)
            game = result.state
            record(describe(current, result.outcome))
            // Whatever happened, the next ask is chosen afresh: a success keeps
            // the turn, a failure hands it on.
            pendingRequest = null
        } else {
            val regionResult = engine.askRegion(current, request.targetPlayerId, quartetId)
            game = regionResult.state
            record(describeRegion(current, regionResult.outcome))
            when (regionResult.outcome) {
                // A "no" ends the turn, so the chosen country is dropped.
                is RegionOutcome.Absent -> pendingRequest = null
                is RegionOutcome.Present -> confirmedThisTurn += pair
            }
        }
        publish()
    }

    /**
     * The table for one game: the human, then three physicists drawn from the
     * roster, so the opponents differ from game to game.
     *
     * The score board only has a quarter of the screen per player, so the seats
     * are named with the short name.
     */
    private fun drawSeats(): List<PlayerSeat> {
        val opponents = repository.physicists()
            .shuffled(random)
            .take(GameEngine.PLAYER_COUNT - 1)
            .map { PlayerSeat(id = it.id, name = it.shortName, isHuman = false) }
        return listOf(PlayerSeat(GameEngine.HUMAN_ID, HUMAN_NAME, isHuman = true)) + opponents
    }

    /** Drops what the computer player in the middle of a turn was doing. */
    private fun forgetAiTurn() {
        confirmedThisTurn.clear()
        pendingRequest = null
        lastAsker = null
    }

    /** Describes an outcome using the names known before the move was applied. */
    private fun describe(before: GameState, outcome: RequestOutcome): GameMessage {
        val askingPlayer = before.player(outcome.askingPlayerId)
        val targetPlayer = before.player(outcome.targetPlayerId)
        val country = gameData.country(outcome.countryId).name
        return when (outcome) {
            is RequestOutcome.Failure -> GameMessage.CardRefused(
                askerName = askingPlayer.name,
                targetName = targetPlayer.name,
                countryName = country,
                askerIsHuman = askingPlayer.isHuman,
                targetIsHuman = targetPlayer.isHuman,
            )
            is RequestOutcome.Success -> outcome.completedQuartetId?.let { quartetId ->
                GameMessage.QuartetCompleted(
                    playerName = askingPlayer.name,
                    targetName = targetPlayer.name,
                    countryName = country,
                    askerIsHuman = askingPlayer.isHuman,
                    targetIsHuman = targetPlayer.isHuman,
                    quartet = gameData.quartet(quartetId),
                    countries = gameData.countriesOf(quartetId),
                )
            } ?: GameMessage.CardReceived(
                askerName = askingPlayer.name,
                targetName = targetPlayer.name,
                countryName = country,
                askerIsHuman = askingPlayer.isHuman,
                targetIsHuman = targetPlayer.isHuman,
            )
        }
    }

    /** Describes a region check using the names known before the move was applied. */
    private fun describeRegion(before: GameState, outcome: RegionOutcome): GameMessage {
        val askingPlayer = before.player(outcome.askingPlayerId)
        val targetPlayer = before.player(outcome.targetPlayerId)
        val quartet = gameData.quartet(outcome.quartetId).name
        return when (outcome) {
            is RegionOutcome.Absent -> GameMessage.RegionAbsent(
                askerName = askingPlayer.name,
                targetName = targetPlayer.name,
                quartetName = quartet,
                askerIsHuman = askingPlayer.isHuman,
                targetIsHuman = targetPlayer.isHuman,
            )
            is RegionOutcome.Present -> GameMessage.RegionPresent(
                askerName = askingPlayer.name,
                targetName = targetPlayer.name,
                quartetName = quartet,
                askerIsHuman = askingPlayer.isHuman,
                targetIsHuman = targetPlayer.isHuman,
            )
        }
    }

    /** Sets the current message and appends it to the game's history. */
    private fun record(msg: GameMessage) {
        message = msg
        history = listOf(msg) + history
    }

    private fun publish() {
        // While a card is owed the picture stays at the state before the failed
        // ask, so the card is only handed over when it is taken.
        val current = pendingTake ?: game ?: return
        recordFinishedGame(current)
        _uiState.value = playingState(
            engine = engine,
            gameData = gameData,
            game = current,
            selection = selection,
            message = message,
            history = history,
            animationsEnabled = settings.settings.value.animationsEnabled,
            autoPlay = autoPlay,
            mustTakeCard = pendingTake != null,
        )
    }

    /**
     * Adds the finished game to the statistics, exactly once.
     *
     * [publish] runs on every state change and on every settings change, so the
     * guard is what keeps a single game from being counted repeatedly.
     */
    private fun recordFinishedGame(current: GameState) {
        if (finishRecorded || !current.isFinished) return
        val human = current.players.first { it.isHuman }
        val outcome = current.outcomeFor(human.id) ?: return
        statistics.recordFinishedGame(outcome, human.score)
        finishRecorded = true
    }

    /**
     * How long a computer player pauses before moving.
     *
     * The pause is what makes the computer turns followable, so it is part of
     * the animation setting and disappears with it.
     */
    private fun aiThinkingDelay(): Long =
        if (settings.settings.value.animationsEnabled) AI_TURN_DELAY_MS else 0L

    companion object {
        /** Short pause so the human can follow the computer turns. */
        const val AI_TURN_DELAY_MS = 900L

        /** What the human's own seat is called on the score board. */
        const val HUMAN_NAME = "You"

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    GameViewModel(
                        repository = CountryRepository(AssetGameDataSource(appContext)),
                        settings = AppGraph.settings(appContext),
                        statistics = AppGraph.statistics(appContext),
                    )
                }
            }
        }
    }
}
