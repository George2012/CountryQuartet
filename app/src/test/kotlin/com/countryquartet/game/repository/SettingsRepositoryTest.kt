package com.countryquartet.game.repository

import com.countryquartet.game.model.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun `sound and animations are on by default`() {
        val settings = InMemorySettingsRepository().settings.value

        assertTrue(settings.soundEnabled)
        assertTrue(settings.animationsEnabled)
    }

    @Test
    fun `each switch changes only its own setting`() {
        val repository = InMemorySettingsRepository()

        repository.setAnimationsEnabled(false)

        assertFalse(repository.settings.value.animationsEnabled)
        assertTrue(repository.settings.value.soundEnabled)

        repository.setSoundEnabled(false)
        repository.setAnimationsEnabled(true)

        assertEquals(GameSettings(soundEnabled = false, animationsEnabled = true), repository.settings.value)
    }

    @Test
    fun `an initial state can be supplied`() {
        val repository = InMemorySettingsRepository(GameSettings(animationsEnabled = false))

        assertFalse(repository.settings.value.animationsEnabled)
    }
}
