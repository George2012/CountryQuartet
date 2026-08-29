package com.countryquartet.game.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.countryquartet.game.AppGraph
import com.countryquartet.game.model.GameSettings
import com.countryquartet.game.model.GameStatistics
import com.countryquartet.game.repository.InMemorySettingsRepository
import com.countryquartet.game.repository.InMemoryStatisticsRepository
import com.countryquartet.game.repository.SettingsRepository
import com.countryquartet.game.repository.StatisticsRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Backs the settings screen.
 *
 * Phase 9 adds persistence and statistics behind the same [SettingsRepository],
 * so this class will not have to change.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository.shared,
    private val statisticsRepository: StatisticsRepository = InMemoryStatisticsRepository(),
) : ViewModel() {

    val settings: StateFlow<GameSettings> = settingsRepository.settings

    val statistics: StateFlow<GameStatistics> = statisticsRepository.statistics

    fun setSoundEnabled(enabled: Boolean) = settingsRepository.setSoundEnabled(enabled)

    fun setAnimationsEnabled(enabled: Boolean) = settingsRepository.setAnimationsEnabled(enabled)

    fun resetStatistics() = statisticsRepository.reset()

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    SettingsViewModel(
                        settingsRepository = AppGraph.settings(appContext),
                        statisticsRepository = AppGraph.statistics(appContext),
                    )
                }
            }
        }
    }
}
