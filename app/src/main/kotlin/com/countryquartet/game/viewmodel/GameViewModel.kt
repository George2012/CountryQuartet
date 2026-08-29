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
import com.countryquartet.game.game.GameEngine
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
    private var aiTurns: Job? = null

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
        finishRecorded = false
        try {
            if (!::gameData.isInitialized) {
                gameData = repository.gameData()
                engine = GameEngine(gameData)
            }
            game = engine.newGame(random = random)
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

    /** Asks the selected opponent for the selected country. */
    fun ask() {
        val current = game ?: return
        val countryId = selection.countryId ?: return
        val opponentId = selection.opponentId ?: return
        if (!isHumanTurn() || !engine.isLegalRequest(current, opponentId, countryId)) return

        val result = engine.ask(current, opponentId, countryId)
        game = result.state
        message = describe(current, result.outcome)
        selection = when (result.outcome) {
            // The card is now owned, so the pick is cleared but the quartet
            // stays selected while the human keeps the turn.
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
        publish()
        continueWithAi()
    }

    private fun isHumanTurn(): Boolean {
        val current = game ?: return false
        return !current.isFinished && current.currentPlayer.isHuman
    }

    /** Plays computer turns until it is the human's turn again or the game ends. */
    private fun continueWithAi() {
        aiTurns?.cancel()
        aiTurns = viewModelScope.launch {
            while (true) {
                val current = game ?: return@launch
                if (current.isFinished || current.currentPlayer.isHuman) return@launch
                delay(aiThinkingDelay())
                val request = ai.chooseRequest(engine, current)
                val result = engine.ask(current, request.targetPlayerId, request.countryId)
                game = result.state
                message = describe(current, result.outcome)
                publish()
            }
        }
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
                targetIsHuman = targetPlayer.isHuman,
            )
            is RequestOutcome.Success -> outcome.completedQuartetId?.let { quartetId ->
                GameMessage.QuartetCompleted(
                    playerName = askingPlayer.name,
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

    private fun publish() {
        val current = game ?: return
        recordFinishedGame(current)
        _uiState.value = playingState(
            engine = engine,
            gameData = gameData,
            game = current,
            selection = selection,
            message = message,
            animationsEnabled = settings.settings.value.animationsEnabled,
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
