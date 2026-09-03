package com.countryquartet.game.ui.theme

/**
 * Region colours: one hue per region, in two shades.
 *
 * Every region owns a hue, and that hue appears twice. The deeper *panel*
 * shade backs the block a region's cards sit in, and the lighter *card* shade
 * backs the country cards themselves. Two shades of one hue keep a card
 * legible against the block it sits on while still naming the same region.
 *
 * Three things make the 13 hues hold apart:
 *
 * The hues are spread evenly around the wheel, then handed out in a stride of
 * 5 rather than in order. 5 and 13 share no factor, so every hue is still
 * used exactly once, but regions that sit next to each other in a hand land
 * 138 degrees apart instead of 28 - neighbouring greens were otherwise easy
 * to confuse.
 *
 * Each colour is then solved for a fixed *perceived* luminance rather than a
 * fixed HSL lightness: yellow is far brighter than blue at the same lightness,
 * so matching lightness would leave the yellow regions washed out and their
 * text harder to read.
 *
 * The four luminance targets are chosen so that body text clears 4.5:1 on
 * every shade in both themes, and so panel and card stay visibly apart.
 * QuartetPaletteTest checks both, so retuning a shade cannot quietly cost
 * readability.
 *
 * Plain ARGB longs rather than Compose colours, so the palette can be checked
 * by a JVM test.
 */
internal object QuartetPalette {

    /**
     * The block a region's cards sit in. The quieter of the two shades: a
     * screen can show all 13 at once, so the panels stay muted and let the
     * cards on top of them carry the colour.
     */
    val panelLight: Map<String, Long> = mapOf(
        "nordic_countries" to 0xFFE9BBBB,
        "southern_europe" to 0xFF82D79C,
        "central_europe" to 0xFFD7BBE9,
        "eastern_europe" to 0xFFD0C96B,
        "middle_east" to 0xFF97CEDE,
        "east_asia" to 0xFFE8B8CE,
        "south_asia" to 0xFF8FD781,
        "southeast_asia" to 0xFFC7C0EB,
        "north_africa" to 0xFFE1BFA2,
        "east_africa" to 0xFF7CD5C1,
        "north_america" to 0xFFE8B5E4,
        "south_america" to 0xFFACD26F,
        "oceania" to 0xFFB6C6E8,
    )

    val panelDark: Map<String, Long> = mapOf(
        "nordic_countries" to 0xFF5C1818,
        "southern_europe" to 0xFF0F371B,
        "central_europe" to 0xFF471A64,
        "eastern_europe" to 0xFF34320E,
        "middle_east" to 0xFF11353F,
        "east_asia" to 0xFF591836,
        "south_asia" to 0xFF15370F,
        "southeast_asia" to 0xFF2E207A,
        "north_africa" to 0xFF452A12,
        "east_africa" to 0xFF0E362D,
        "north_america" to 0xFF53164F,
        "south_america" to 0xFF26360E,
        "oceania" to 0xFF192F5F,
    )

    /** The country card itself: the fuller shade, a few steps clear of its own panel. */
    val cardLight: Map<String, Long> = mapOf(
        "nordic_countries" to 0xFFFEE7E7,
        "southern_europe" to 0xFFBBFBCE,
        "central_europe" to 0xFFF5E7FE,
        "eastern_europe" to 0xFFF8EF83,
        "middle_east" to 0xFFCFF2FC,
        "east_asia" to 0xFFFEE6F1,
        "south_asia" to 0xFFC2FBB8,
        "southeast_asia" to 0xFFEDEAFE,
        "north_africa" to 0xFFFDE8D7,
        "east_africa" to 0xFFB0FBE9,
        "north_america" to 0xFFFEE5FC,
        "south_america" to 0xFFD3F996,
        "oceania" to 0xFFE5EDFE,
    )

    val cardDark: Map<String, Long> = mapOf(
        "nordic_countries" to 0xFF952727,
        "southern_europe" to 0xFF185C2D,
        "central_europe" to 0xFF742BA1,
        "eastern_europe" to 0xFF565217,
        "middle_east" to 0xFF1C5768,
        "east_asia" to 0xFF902657,
        "south_asia" to 0xFF235C18,
        "southeast_asia" to 0xFF4B35C6,
        "north_africa" to 0xFF73451F,
        "east_africa" to 0xFF185A4A,
        "north_america" to 0xFF882480,
        "south_america" to 0xFF3F5717,
        "oceania" to 0xFF294C9A,
    )

    /** Falls back to a neutral tint for a region that is not in the palette. */
    fun panelColorFor(quartetId: String, isDark: Boolean): Long =
        if (isDark) {
            panelDark[quartetId] ?: FALLBACK_PANEL_DARK
        } else {
            panelLight[quartetId] ?: FALLBACK_PANEL_LIGHT
        }

    fun cardColorFor(quartetId: String, isDark: Boolean): Long =
        if (isDark) {
            cardDark[quartetId] ?: FALLBACK_CARD_DARK
        } else {
            cardLight[quartetId] ?: FALLBACK_CARD_LIGHT
        }

    const val FALLBACK_PANEL_LIGHT = 0xFFCED4DE
    const val FALLBACK_PANEL_DARK = 0xFF32363C
    const val FALLBACK_CARD_LIGHT = 0xFFF3F5F9
    const val FALLBACK_CARD_DARK = 0xFF484D55
}

/** The colour of the block [quartetId]'s cards sit in, for the theme in use. */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun quartetBackground(quartetId: String): androidx.compose.ui.graphics.Color =
    androidx.compose.ui.graphics.Color(
        QuartetPalette.panelColorFor(
            quartetId = quartetId,
            isDark = androidx.compose.foundation.isSystemInDarkTheme(),
        ),
    )

/** The colour of a single country card from [quartetId], for the theme in use. */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun quartetCardBackground(quartetId: String): androidx.compose.ui.graphics.Color =
    androidx.compose.ui.graphics.Color(
        QuartetPalette.cardColorFor(
            quartetId = quartetId,
            isDark = androidx.compose.foundation.isSystemInDarkTheme(),
        ),
    )
