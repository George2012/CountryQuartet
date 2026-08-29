package com.countryquartet.game.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.countryquartet.game.data.PreferenceKeys
import com.countryquartet.game.model.GameSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Settings stored on the device with DataStore.
 *
 * Reads that fail fall back to the defaults instead of taking the app down: a
 * damaged preferences file is not a reason to stop a child playing a card game.
 */
class DataStoreSettingsRepository(
    private val store: DataStore<Preferences>,
    private val scope: CoroutineScope,
) : SettingsRepository {

    override val settings: StateFlow<GameSettings> = store.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { preferences ->
            GameSettings(
                soundEnabled = preferences[PreferenceKeys.SOUND_ENABLED] ?: DEFAULT.soundEnabled,
                animationsEnabled = preferences[PreferenceKeys.ANIMATIONS_ENABLED]
                    ?: DEFAULT.animationsEnabled,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT)

    override fun setSoundEnabled(enabled: Boolean) {
        scope.launch { store.edit { it[PreferenceKeys.SOUND_ENABLED] = enabled } }
    }

    override fun setAnimationsEnabled(enabled: Boolean) {
        scope.launch { store.edit { it[PreferenceKeys.ANIMATIONS_ENABLED] = enabled } }
    }

    private companion object {
        val DEFAULT = GameSettings()
    }
}
