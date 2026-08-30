package com.countryquartet.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The flag of a country, drawn from its ISO code.
 *
 * The glyph is scaled to fill its frame rather than sitting inside it with a
 * margin: an emoji is drawn well inside its em box, so asking for a 40dp font
 * gives a flag closer to 30dp. [GLYPH_FILL] compensates, the frame clips
 * whatever overflows, and the font size is derived from the frame in dp so the
 * flag keeps its size whatever the system font scale is set to.
 *
 * Devices whose system font has no flag emoji show the two letter code
 * instead, which is why the flag always sits inside a framed box: the fallback
 * still reads as a deliberate badge rather than broken text.
 */
@Composable
fun CountryFlag(
    countryId: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val flag = flagEmoji(countryId)
    val density = LocalDensity.current
    val glyphSize = with(density) { (size * GLYPH_FILL).toSp() }
    val codeSize = with(density) { (size * 0.42f).toSp() }

    Box(
        modifier = modifier
            .size(width = size * FLAG_RATIO, height = size)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = flag.ifEmpty { countryId.uppercase() },
            fontSize = if (flag.isEmpty()) codeSize else glyphSize,
            lineHeight = glyphSize,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                // Without this the glyph is pushed off centre by font metrics
                // and the frame gains padding it was not asked for.
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}

/** Flag emoji are about this much wider than tall. */
private const val FLAG_RATIO = 1.4f

/** An emoji fills roughly three quarters of its em box, so ask for more. */
private const val GLYPH_FILL = 1.3f
