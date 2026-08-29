package com.countryquartet.game.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the player wants animations.
 *
 * Provided once at the top of a screen so every card and panel below can honour
 * the setting without it being threaded through every parameter list.
 */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/**
 * Timings for the whole app. They are deliberately short: feedback in a card
 * game should confirm what happened, not make the player wait for it.
 */
object Motion {

    /** Selection and colour changes. */
    const val QUICK_MS = 150

    /** Panels and banners appearing. */
    const val ENTER_MS = 250

    /** How long the "quartet completed" banner stays up. */
    const val BANNER_MS = 1800L

    /** A spec that collapses to an instant change when animations are off. */
    @Composable
    @ReadOnlyComposable
    fun <T> spec(durationMillis: Int = QUICK_MS): AnimationSpec<T> =
        if (LocalAnimationsEnabled.current) tween(durationMillis) else snap()
}
