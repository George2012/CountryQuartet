package com.countryquartet.game.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.countryquartet.game.ai.AiStrategy
import com.countryquartet.game.ai.BasicAi
import com.countryquartet.game.data.AssetGameDataSource
import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.game.RequestOutcome
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.GameState
import com.countryquartet.game.repository.CountryRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var gameData: GameData
    private lateinit var engine: GameEngine

    private var game: GameState? = null
    private var selection = Selection()
    private var message: GameMessage? = null
    private var aiTurns: Job? = null

    init {
        startNewGame()
    }

    /** Deals a fresh game. Used on entry and by "Play again". */
    fun startNewGame() {
        aiTurns?.cancel()
        selection = Selection()
        message = null
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
                delay(AI_TURN_DELAY_MS)
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
        val asker = before.player(outcome.askingPlayerId).name
        val target = before.player(outcome.targetPlayerId).name
        val country = gameData.country(outcome.countryId).name
        return when (outcome) {
            is RequestOutcome.Failure -> GameMessage.CardRefused(asker, target, country)
            is RequestOutcome.Success -> outcome.completedQuartetId?.let { quartetId ->
                GameMessage.QuartetCompleted(asker, gameData.quartet(quartetId).name)
            } ?: GameMessage.CardReceived(asker, target, country)
        }
    }

    private fun publish() {
        val current = game ?: return
        _uiState.value = playingState(engine, gameData, current, selection, message)
    }

    companion object {
        /** Short pause so the human can follow the computer turns. Refined in Phase 7. */
        const val AI_TURN_DELAY_MS = 900L

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    GameViewModel(CountryRepository(AssetGameDataSource(appContext)))
                }
            }
        }
    }
}
