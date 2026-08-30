package com.countryquartet.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The small card used in lists and in the hand.
 *
 * The flag is the largest thing on the card and carries the recognition; the
 * country name and its capital sit underneath in the same size, so the pair
 * reads as two lines of one label rather than a heading and a footnote.
 *
 * Same states and same frame as [CountryCard], so a selected card looks
 * selected everywhere.
 */
@Composable
fun CompactCountryCard(
    countryId: String,
    name: String,
    capital: String?,
    modifier: Modifier = Modifier,
    state: CardState = CardState.Normal,
    onClick: (() -> Unit)? = null,
) {
    CardSurface(
        state = state,
        onClick = onClick,
        modifier = modifier.width(CARD_WIDTH),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CountryFlag(countryId = countryId, size = FLAG_HEIGHT, fontSize = FLAG_GLYPH)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (state == CardState.Selected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (capital != null) {
                    Text(
                        // Same size as the country name, told apart by colour.
                        text = capital,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Wide enough for "Washington, D.C." on one line at the default font scale. */
private val CARD_WIDTH = 104.dp
private val FLAG_HEIGHT = 40.dp
private val FLAG_GLYPH = 28.sp
