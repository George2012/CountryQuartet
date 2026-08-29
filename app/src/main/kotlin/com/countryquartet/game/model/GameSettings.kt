package com.countryquartet.game.model

/**
 * Player preferences.
 *
 * Phase 7 keeps these in memory; Phase 9 will store them with DataStore
 * without the rest of the app having to change.
 */
data class GameSettings(
    val soundEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
)
