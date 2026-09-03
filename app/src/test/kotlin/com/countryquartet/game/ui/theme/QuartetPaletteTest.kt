package com.countryquartet.game.ui.theme

import androidx.compose.ui.graphics.Color
import com.countryquartet.game.game.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class QuartetPaletteTest {

    private val quartetIds = TestGame.gameData.quartets.map { it.id }

    private val shades = mapOf(
        "light panel" to QuartetPalette.panelLight,
        "light card" to QuartetPalette.cardLight,
        "dark panel" to QuartetPalette.panelDark,
        "dark card" to QuartetPalette.cardDark,
    )

    @Test
    fun `every region has all four shades`() {
        shades.forEach { (name, shade) ->
            assertEquals(name, emptyList<String>(), quartetIds.filterNot { it in shade })
        }
    }

    @Test
    fun `no two regions share a shade`() {
        // The point of the palette is telling regions apart, so a duplicate
        // would quietly defeat it.
        shades.forEach { (name, shade) ->
            val used = quartetIds.map { shade.getValue(it) }
            assertEquals(name, quartetIds.size, used.distinct().size)
        }
    }

    @Test
    fun `panel and card stay apart within a region`() {
        // A country card sits directly on its own region's panel and shares its
        // hue, so only the shade separates the two.
        quartetIds.forEach { id ->
            assertContrast("$id light", QuartetPalette.panelLight, QuartetPalette.cardLight, id, 1.4)
            assertContrast("$id dark", QuartetPalette.panelDark, QuartetPalette.cardDark, id, 1.4)
        }
    }

    @Test
    fun `body text clears 4_5 to 1 on every shade`() {
        // Every shade carries card text, so retuning one for punch must not
        // cost readability. 4.5:1 is the WCAG AA floor for normal text.
        val onLight = listOf(LightOnSurface, LightOnSurfaceVariant)
        val onDark = listOf(DarkOnSurface, DarkOnSurfaceVariant)
        quartetIds.forEach { id ->
            listOf(QuartetPalette.panelLight, QuartetPalette.cardLight).forEach { shade ->
                onLight.forEach { text -> assertTextContrast(id, shade.getValue(id), text) }
            }
            listOf(QuartetPalette.panelDark, QuartetPalette.cardDark).forEach { shade ->
                onDark.forEach { text -> assertTextContrast(id, shade.getValue(id), text) }
            }
        }
    }

    @Test
    fun `an unknown region falls back instead of failing`() {
        assertEquals(
            QuartetPalette.FALLBACK_PANEL_LIGHT,
            QuartetPalette.panelColorFor("atlantis", isDark = false),
        )
        assertEquals(
            QuartetPalette.FALLBACK_PANEL_DARK,
            QuartetPalette.panelColorFor("atlantis", isDark = true),
        )
        assertEquals(
            QuartetPalette.FALLBACK_CARD_LIGHT,
            QuartetPalette.cardColorFor("atlantis", isDark = false),
        )
        assertEquals(
            QuartetPalette.FALLBACK_CARD_DARK,
            QuartetPalette.cardColorFor("atlantis", isDark = true),
        )
    }

    private fun assertContrast(
        what: String,
        panel: Map<String, Long>,
        card: Map<String, Long>,
        id: String,
        least: Double,
    ) {
        val ratio = contrast(luminance(panel.getValue(id)), luminance(card.getValue(id)))
        assertTrue("$what panel/card contrast $ratio", ratio >= least)
    }

    private fun assertTextContrast(id: String, background: Long, text: Color) {
        val ratio = contrast(luminance(background), luminance(text))
        assertTrue("$id text contrast $ratio", ratio >= 4.5)
    }

    private fun contrast(a: Double, b: Double): Double {
        val lighter = maxOf(a, b)
        val darker = minOf(a, b)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** WCAG relative luminance, so the check matches what a reader perceives. */
    private fun luminance(argb: Long): Double = luminance(
        red = ((argb shr 16) and 0xFF) / 255.0,
        green = ((argb shr 8) and 0xFF) / 255.0,
        blue = (argb and 0xFF) / 255.0,
    )

    private fun luminance(color: Color): Double =
        luminance(color.red.toDouble(), color.green.toDouble(), color.blue.toDouble())

    private fun luminance(red: Double, green: Double, blue: Double): Double {
        fun channel(value: Double) =
            if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
    }
}
