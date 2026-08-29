package com.countryquartet.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The small card used in lists: flag, country name and its capital.
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
        modifier = modifier.widthIn(min = 104.dp, max = 168.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountryFlag(countryId = countryId, size = 24.dp, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (state == CardState.Selected) FontWeight.Bold else FontWeight.Normal,
                    // "Papua New Guinea" does not fit on one line at this width,
                    // and a truncated country name is useless in a geography game.
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (capital != null) {
                    Text(
                        text = capital,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // Sri Jayawardenepura Kotte needs a second line.
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
