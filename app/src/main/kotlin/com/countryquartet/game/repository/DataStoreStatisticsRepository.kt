package com.countryquartet.game.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.countryquartet.game.data.PreferenceKeys
import com.countryquartet.game.model.GameOutcome
import com.countryquartet.game.model.GameStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

/** Statistics stored on the device with DataStore. */
class DataStoreStatisticsRepository(
    private val store: DataStore<Preferences>,
    private val scope: CoroutineScope,
) : StatisticsRepository {

    override val statistics: StateFlow<GameStatistics> = store.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { preferences ->
            GameStatistics(
                gamesPlayed = preferences[PreferenceKeys.GAMES_PLAYED] ?: 0,
                gamesWon = preferences[PreferenceKeys.GAMES_WON] ?: 0,
                gamesLost = preferences[PreferenceKeys.GAMES_LOST] ?: 0,
                draws = preferences[PreferenceKeys.DRAWS] ?: 0,
                totalQuartets = preferences[PreferenceKeys.TOTAL_QUARTETS] ?: 0,
                bestScore = preferences[PreferenceKeys.BEST_SCORE] ?: 0,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, GameStatistics())

    override fun recordFinishedGame(outcome: GameOutcome, quartetsCollected: Int) {
        scope.launch {
            store.edit { preferences ->
                // Read, apply the same pure rule as the in-memory version, write.
                val updated = GameStatistics(
                    gamesPlayed = preferences[PreferenceKeys.GAMES_PLAYED] ?: 0,
                    gamesWon = preferences[PreferenceKeys.GAMES_WON] ?: 0,
                    gamesLost = preferences[PreferenceKeys.GAMES_LOST] ?: 0,
                    draws = preferences[PreferenceKeys.DRAWS] ?: 0,
                    totalQuartets = preferences[PreferenceKeys.TOTAL_QUARTETS] ?: 0,
                    bestScore = preferences[PreferenceKeys.BEST_SCORE] ?: 0,
                ).recording(outcome, quartetsCollected)

                preferences[PreferenceKeys.GAMES_PLAYED] = updated.gamesPlayed
                preferences[PreferenceKeys.GAMES_WON] = updated.gamesWon
                preferences[PreferenceKeys.GAMES_LOST] = updated.gamesLost
                preferences[PreferenceKeys.DRAWS] = updated.draws
                preferences[PreferenceKeys.TOTAL_QUARTETS] = updated.totalQuartets
                preferences[PreferenceKeys.BEST_SCORE] = updated.bestScore
            }
        }
    }

    override fun reset() {
        scope.launch {
            store.edit { preferences ->
                preferences.remove(PreferenceKeys.GAMES_PLAYED)
                preferences.remove(PreferenceKeys.GAMES_WON)
                preferences.remove(PreferenceKeys.GAMES_LOST)
                preferences.remove(PreferenceKeys.DRAWS)
                preferences.remove(PreferenceKeys.TOTAL_QUARTETS)
                preferences.remove(PreferenceKeys.BEST_SCORE)
            }
        }
    }
}
