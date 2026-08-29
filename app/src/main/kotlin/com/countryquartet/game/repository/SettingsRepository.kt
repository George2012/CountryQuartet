package com.countryquartet.game.repository

import com.countryquartet.game.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the player preferences.
 *
 * The interface is what the game and the settings screen depend on, so Phase 9
 * can swap the in-memory implementation for a DataStore backed one.
 */
interface SettingsRepository {

    val settings: StateFlow<GameSettings>

    fun setSoundEnabled(enabled: Boolean)

    fun setAnimationsEnabled(enabled: Boolean)
}

/**
 * Keeps the preferences for as long as the process lives.
 *
 * A single shared instance is used so a change on the settings screen is seen
 * by a game that is already running.
 */
class InMemorySettingsRepository(
    initial: GameSettings = GameSettings(),
) : SettingsRepository {

    private val _settings = MutableStateFlow(initial)
    override val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    override fun setSoundEnabled(enabled: Boolean) {
        _settings.update { it.copy(soundEnabled = enabled) }
    }

    override fun setAnimationsEnabled(enabled: Boolean) {
        _settings.update { it.copy(animationsEnabled = enabled) }
    }

    companion object {
        /**
         * Process wide instance.
         *
         * Phase 9 replaces this with a DataStore backed repository provided the
         * same way, so nothing else has to change.
         */
        val shared: SettingsRepository by lazy { InMemorySettingsRepository() }
    }
}
