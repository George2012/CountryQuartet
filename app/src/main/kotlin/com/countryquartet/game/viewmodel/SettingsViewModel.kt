package com.countryquartet.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.countryquartet.game.model.GameSettings
import com.countryquartet.game.repository.InMemorySettingsRepository
import com.countryquartet.game.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Backs the settings screen.
 *
 * Phase 9 adds persistence and statistics behind the same [SettingsRepository],
 * so this class will not have to change.
 */
class SettingsViewModel(
    private val repository: SettingsRepository = InMemorySettingsRepository.shared,
) : ViewModel() {

    val settings: StateFlow<GameSettings> = repository.settings

    fun setSoundEnabled(enabled: Boolean) = repository.setSoundEnabled(enabled)

    fun setAnimationsEnabled(enabled: Boolean) = repository.setAnimationsEnabled(enabled)

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel() }
        }
    }
}
