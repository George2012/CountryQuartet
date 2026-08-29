package com.countryquartet.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.countryquartet.game.R
import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Quartet
import com.countryquartet.game.ui.theme.CountryQuartetTheme
import androidx.compose.ui.res.stringResource

/**
 * The full country card: flag, name, quartet, the three facts, a fun fact and
 * the "collect all 4" list.
 *
 * [ownedCountryIds] marks which of the four the player already holds, so the
 * card always answers "what do I still need?" at a glance.
 */
@Composable
fun CountryCard(
    country: Country,
    quartet: Quartet,
    quartetCountries: List<Country>,
    modifier: Modifier = Modifier,
    state: CardState = CardState.Normal,
    ownedCountryIds: Set<String> = emptySet(),
    onClick: (() -> Unit)? = null,
) {
    CardSurface(state = state, onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = quartet.name.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CountryFlag(countryId = country.id, size = 48.dp)
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FactRow(stringResource(R.string.card_capital), country.capital)
                FactRow(stringResource(R.string.card_language), country.language)
                FactRow(stringResource(R.string.card_currency), country.currency)
            }

            Text(
                text = country.funFact,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CollectAllFour(
                countries = quartetCountries,
                currentCountryId = country.id,
                ownedCountryIds = ownedCountryIds,
            )
        }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * The four countries of the quartet. The card's own country is starred, cards
 * the player holds are ticked and everything else is still to collect.
 */
@Composable
fun CollectAllFour(
    countries: List<Country>,
    currentCountryId: String?,
    ownedCountryIds: Set<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(R.string.card_collect_all_four),
            style = MaterialTheme.typography.labelLarge,
        )
        countries.forEach { country ->
            val isCurrent = country.id == currentCountryId
            val isOwned = country.id in ownedCountryIds
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when {
                        isCurrent -> "★"
                        isOwned -> "✓"
                        else -> "○"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrent || isOwned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
                Text(
                    text = if (isCurrent) country.name.uppercase() else country.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent || isOwned) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

/** Shared frame so every card state looks the same across screens. */
@Composable
internal fun CardSurface(
    state: CardState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Picking a card is the main thing a player does, so the change of colour
    // and lift is animated - and snaps instead when animations are switched off.
    val targetColor = when (state) {
        CardState.Selected -> MaterialTheme.colorScheme.secondaryContainer
        CardState.Requested -> MaterialTheme.colorScheme.tertiaryContainer
        CardState.Owned -> MaterialTheme.colorScheme.surfaceVariant
        CardState.Disabled -> MaterialTheme.colorScheme.surfaceVariant
        CardState.Normal -> MaterialTheme.colorScheme.surface
    }
    val containerColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = Motion.spec(),
        label = "cardContainerColor",
    )
    val lift by animateDpAsState(
        targetValue = if (state == CardState.Selected) 6.dp else 1.dp,
        animationSpec = Motion.spec(),
        label = "cardElevation",
    )
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = lift)
    if (onClick == null) {
        Card(modifier = modifier, colors = colors, elevation = elevation) { content() }
    } else {
        Card(
            onClick = onClick,
            enabled = state != CardState.Disabled,
            modifier = modifier,
            colors = colors,
            elevation = elevation,
        ) { content() }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CountryCardPreview() {
    val sweden = Country(
        id = "se",
        name = "Sweden",
        quartetId = "nordic_countries",
        capital = "Stockholm",
        language = "Swedish",
        currency = "Swedish krona",
        flagAsset = "flags/se.png",
        funFact = "Sweden has around 267,000 islands, more than any other country.",
    )
    val quartet = Quartet("nordic_countries", "Nordic Countries", listOf("se", "no", "dk", "fi"))
    CountryQuartetTheme {
        CountryCard(
            country = sweden,
            quartet = quartet,
            quartetCountries = listOf(sweden),
            ownedCountryIds = setOf("se"),
        )
    }
}
