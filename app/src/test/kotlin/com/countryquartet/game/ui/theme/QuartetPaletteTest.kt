package com.countryquartet.game.ui.theme

import com.countryquartet.game.game.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuartetPaletteTest {

    private val quartetIds = TestGame.gameData.quartets.map { it.id }

    @Test
    fun `every region has a colour in both themes`() {
        val missingLight = quartetIds.filterNot { it in QuartetPalette.light }
        val missingDark = quartetIds.filterNot { it in QuartetPalette.dark }

        assertEquals(emptyList<String>(), missingLight)
        assertEquals(emptyList<String>(), missingDark)
    }

    @Test
    fun `no two regions share a colour`() {
        // The point of the palette is telling regions apart, so a duplicate
        // would quietly defeat it.
        assertEquals(quartetIds.size, quartetIds.map { QuartetPalette.light.getValue(it) }.distinct().size)
        assertEquals(quartetIds.size, quartetIds.map { QuartetPalette.dark.getValue(it) }.distinct().size)
    }

    @Test
    fun `light and dark differ for every region`() {
        quartetIds.forEach { id ->
            assertNotEquals(id, QuartetPalette.light.getValue(id), QuartetPalette.dark.getValue(id))
        }
    }

    @Test
    fun `light colours are light and dark colours are dark`() {
        fun luminance(argb: Long): Double {
            val r = ((argb shr 16) and 0xFF) / 255.0
            val g = ((argb shr 8) and 0xFF) / 255.0
            val b = (argb and 0xFF) / 255.0
            return 0.2126 * r + 0.7152 * g + 0.0722 * b
        }
        quartetIds.forEach { id ->
            val light = luminance(QuartetPalette.light.getValue(id))
            val dark = luminance(QuartetPalette.dark.getValue(id))
            // Dark text sits on the light tints and light text on the dark ones,
            // so each has to stay on its own side.
            assertTrue("$id light luminance $light", light > 0.7)
            assertTrue("$id dark luminance $dark", dark < 0.3)
        }
    }

    @Test
    fun `an unknown region falls back instead of failing`() {
        assertEquals(QuartetPalette.FALLBACK_LIGHT, QuartetPalette.colorFor("atlantis", isDark = false))
        assertEquals(QuartetPalette.FALLBACK_DARK, QuartetPalette.colorFor("atlantis", isDark = true))
    }
}
