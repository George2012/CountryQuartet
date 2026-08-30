package com.countryquartet.game.ui.components

import com.countryquartet.game.game.TestGame
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

class FlagDrawablesTest {

    @Test
    fun `every country in the dataset has a bundled flag`() {
        val missing = TestGame.gameData.countries
            .filter { flagDrawable(it.id) == null }
            .map { "${it.id} (${it.name})" }

        assertEquals("countries with no flag image", emptyList<String>(), missing)
    }

    @Test
    fun `no two countries share a flag image`() {
        val drawables = TestGame.gameData.countries.mapNotNull { flagDrawable(it.id) }

        assertEquals(52, drawables.size)
        assertEquals(52, drawables.distinct().size)
    }

    @Test
    fun `the lookup accepts any case`() {
        assertEquals(flagDrawable("se"), flagDrawable("SE"))
        assertNotNull(flagDrawable("Se"))
    }

    @Test
    fun `an unknown country has no flag rather than a wrong one`() {
        assertNull(flagDrawable("atlantis"))
        assertNull(flagDrawable(""))
    }
}
