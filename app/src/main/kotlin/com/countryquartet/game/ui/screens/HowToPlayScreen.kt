package com.countryquartet.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.countryquartet.game.R
import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.ui.components.CountryFlag
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.CountryQuartetTheme
import com.countryquartet.game.ui.theme.quartetBackground

/**
 * The five steps of a turn, in the order a new player meets them.
 *
 * [titleArgs] lets a step quote a number the engine owns rather than repeating
 * it in the string. The hand size shipped as a hardcoded "13" here while the
 * engine dealt six, so the rules screen taught the wrong game.
 */
private data class HowToStep(
    val titleRes: Int,
    val bodyRes: Int,
    val titleArgs: List<Any> = emptyList(),
)

private val steps = listOf(
    HowToStep(
        R.string.howto_step1_title,
        R.string.howto_step1_body,
        titleArgs = listOf(GameEngine.DEFAULT_CARDS_PER_PLAYER),
    ),
    HowToStep(R.string.howto_step2_title, R.string.howto_step2_body),
    HowToStep(R.string.howto_step3_title, R.string.howto_step3_body),
    HowToStep(R.string.howto_step4_title, R.string.howto_step4_body),
    HowToStep(R.string.howto_step5_title, R.string.howto_step5_body),
)

/** The four Nordic countries, used as the worked example. */
private const val EXAMPLE_QUARTET_ID = "nordic_countries"
private val exampleCountries = listOf("se" to "Sweden", "no" to "Norway", "dk" to "Denmark", "fi" to "Finland")

@Composable
fun HowToPlayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = stringResource(R.string.title_how_to_play),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.howto_intro),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item { ExampleQuartet() }
            steps.forEachIndexed { index, step ->
                item(key = step.titleRes) { StepRow(number = index + 1, step = step) }
            }
            item {
                Text(
                    text = stringResource(R.string.howto_tip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/** Shows a real quartet, so "a region of four" is concrete before the rules start. */
@Composable
private fun ExampleQuartet() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // The region's own colour, not a generic accent: the example names
        // Nordic Countries, and the player will meet that same colour in their
        // hand a moment later.
        colors = CardDefaults.cardColors(
            containerColor = quartetBackground(EXAMPLE_QUARTET_ID),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.howto_example),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(text = "Nordic Countries", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                exampleCountries.forEach { (id, name) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CountryFlag(countryId = id, size = 26.dp)
                        Text(text = name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, step: HowToStep) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(step.titleRes, *step.titleArgs.toTypedArray()),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(step.bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HowToPlayScreenPreview() {
    CountryQuartetTheme {
        HowToPlayScreen(onBack = {})
    }
}
