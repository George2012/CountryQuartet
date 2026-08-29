package com.countryquartet.game

import android.content.Context
import com.countryquartet.game.data.gamePreferences
import com.countryquartet.game.repository.DataStoreSettingsRepository
import com.countryquartet.game.repository.DataStoreStatisticsRepository
import com.countryquartet.game.repository.SettingsRepository
import com.countryquartet.game.repository.StatisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The app's shared objects.
 *
 * A hand written locator rather than a dependency injection framework: there
 * are two repositories and one scope, and keeping the dependency list short
 * matters more here than the ceremony would be worth.
 */
object AppGraph {

    /** Outlives any screen, so stored values keep flowing across navigation. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var settingsRepository: SettingsRepository? = null

    @Volatile
    private var statisticsRepository: StatisticsRepository? = null

    fun settings(context: Context): SettingsRepository =
        settingsRepository ?: synchronized(this) {
            settingsRepository ?: DataStoreSettingsRepository(
                store = context.applicationContext.gamePreferences,
                scope = applicationScope,
            ).also { settingsRepository = it }
        }

    fun statistics(context: Context): StatisticsRepository =
        statisticsRepository ?: synchronized(this) {
            statisticsRepository ?: DataStoreStatisticsRepository(
                store = context.applicationContext.gamePreferences,
                scope = applicationScope,
            ).also { statisticsRepository = it }
        }
}
