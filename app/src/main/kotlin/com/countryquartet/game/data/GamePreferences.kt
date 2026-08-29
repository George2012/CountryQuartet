package com.countryquartet.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** The single preferences file holding settings and statistics. */
val Context.gamePreferences: DataStore<Preferences> by preferencesDataStore(name = "country_quartet")

/** Keys of everything stored on the device. */
object PreferenceKeys {
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")

    val GAMES_PLAYED = intPreferencesKey("games_played")
    val GAMES_WON = intPreferencesKey("games_won")
    val GAMES_LOST = intPreferencesKey("games_lost")
    val DRAWS = intPreferencesKey("draws")
    val TOTAL_QUARTETS = intPreferencesKey("total_quartets")
    val BEST_SCORE = intPreferencesKey("best_score")
}
