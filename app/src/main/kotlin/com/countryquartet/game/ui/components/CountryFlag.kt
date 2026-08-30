package com.countryquartet.game.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The flag of a country.
 *
 * Drawn from a bundled image, so the flag is a flat rectangle in its own true
 * proportions rather than the waving shape an emoji font produces. The height
 * is fixed and the width follows the flag's real aspect ratio, which means the
 * image fills its frame exactly: no dead space, no stretching, and nothing
 * cropped off a flag whose canton sits in a corner.
 *
 * A country with no bundled image falls back to the flag emoji, and then to
 * its two letter code, so an added country can never render as a blank.
 */
@Composable
fun CountryFlag(
    countryId: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val drawable = flagDrawable(countryId)
    val shape = RoundedCornerShape(3.dp)

    if (drawable != null) {
        val painter = painterResource(drawable)
        val intrinsic = painter.intrinsicSize
        val ratio = if (intrinsic.isSpecified && intrinsic.height > 0f) {
            intrinsic.width / intrinsic.height
        } else {
            DEFAULT_RATIO
        }
        Box(
            modifier = modifier
                .size(width = size * ratio, height = size)
                .clip(shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                // The frame already matches the flag's aspect, so filling the
                // bounds cannot distort it.
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        FallbackFlag(countryId = countryId, size = size, shape = shape, modifier = modifier)
    }
}

@Composable
private fun FallbackFlag(
    countryId: String,
    size: Dp,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
) {
    val flag = flagEmoji(countryId)
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .size(width = size * DEFAULT_RATIO, height = size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = flag.ifEmpty { countryId.uppercase() },
            fontSize = with(density) { (size * if (flag.isEmpty()) 0.42f else 0.8f).toSp() },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/** Used only by the fallback, where the real proportions are unknown. */
private const val DEFAULT_RATIO = 1.5f
