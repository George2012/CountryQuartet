package com.countryquartet.game.ui.theme

/**
 * One background colour per region, so a quartet can be told from its
 * neighbour at a glance without reading its name.
 *
 * The hues are spread evenly around the colour wheel rather than chosen by
 * hand, which is what keeps all 13 of them apart. Each colour is then solved
 * for a fixed *perceived* luminance rather than a fixed HSL lightness: yellow
 * is far brighter than blue at the same lightness, so matching lightness would
 * leave the yellow regions washed out and their text harder to read.
 *
 * Plain ARGB longs rather than Compose colours, so the palette can be checked
 * by a JVM test.
 */
internal object QuartetPalette {

    val light: Map<String, Long> = mapOf(
        "nordic_countries" to 0xFFF4CECE,
        "southern_europe" to 0xFFEFD2B9,
        "central_europe" to 0xFFE2DB84,
        "eastern_europe" to 0xFFC2E48B,
        "middle_east" to 0xFFABE9A0,
        "east_asia" to 0xFFA1E9B7,
        "south_asia" to 0xFF9CE8D6,
        "southeast_asia" to 0xFFB1DFED,
        "north_africa" to 0xFFCAD7F3,
        "east_africa" to 0xFFD8D3F5,
        "north_america" to 0xFFE5CFF4,
        "south_america" to 0xFFF3CBF0,
        "oceania" to 0xFFF3CDDE,
    )

    val dark: Map<String, Long> = mapOf(
        "nordic_countries" to 0xFF291515,
        "southern_europe" to 0xFF211811,
        "central_europe" to 0xFF1B1A0E,
        "eastern_europe" to 0xFF161C0E,
        "middle_east" to 0xFF111D0F,
        "east_asia" to 0xFF0F1D13,
        "south_asia" to 0xFF0F1D19,
        "southeast_asia" to 0xFF101C1F,
        "north_africa" to 0xFF141A27,
        "east_africa" to 0xFF1B172D,
        "north_america" to 0xFF22152A,
        "south_america" to 0xFF271426,
        "oceania" to 0xFF28151E,
    )

    /** Falls back to a neutral tint for a region that is not in the palette. */
    fun colorFor(quartetId: String, isDark: Boolean): Long {
        val palette = if (isDark) dark else light
        return palette[quartetId] ?: if (isDark) FALLBACK_DARK else FALLBACK_LIGHT
    }

    const val FALLBACK_LIGHT = 0xFFE4E7EC
    const val FALLBACK_DARK = 0xFF32363C
}

/** The background colour of [quartetId], for the theme currently in use. */
@androidx.compose.runtime.Composable
@androidx.compose.runtime.ReadOnlyComposable
fun quartetBackground(quartetId: String): androidx.compose.ui.graphics.Color =
    androidx.compose.ui.graphics.Color(
        QuartetPalette.colorFor(
            quartetId = quartetId,
            isDark = androidx.compose.foundation.isSystemInDarkTheme(),
        ),
    )
