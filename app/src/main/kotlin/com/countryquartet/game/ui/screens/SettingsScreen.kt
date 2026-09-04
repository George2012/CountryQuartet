package com.countryquartet.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countryquartet.game.R
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.CountryQuartetTheme
import com.countryquartet.game.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(LocalContext.current),
    ),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    var confirmingReset by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold(
        title = stringResource(R.string.title_settings),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // There is no Sound row because the game plays no sounds. A switch
            // that changes nothing is worse than no switch. The setting is still
            // stored and honoured by the repository, so the row can come back
            // unchanged on the day there is something to mute.
            SettingRow(
                title = stringResource(R.string.settings_animations),
                description = stringResource(R.string.settings_animations_hint),
                checked = settings.animationsEnabled,
                onCheckedChange = viewModel::setAnimationsEnabled,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleMedium,
            )
            StatisticRow(stringResource(R.string.stats_games_played), statistics.gamesPlayed)
            StatisticRow(stringResource(R.string.stats_games_won), statistics.gamesWon)
            StatisticRow(stringResource(R.string.stats_games_lost), statistics.gamesLost)
            StatisticRow(stringResource(R.string.stats_draws), statistics.draws)
            StatisticRow(stringResource(R.string.stats_total_quartets), statistics.totalQuartets)
            StatisticRow(stringResource(R.string.stats_best_score), statistics.bestScore)

            OutlinedButton(
                onClick = { confirmingReset = true },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.stats_reset))
            }
        }
    }

    if (confirmingReset) {
        // Clearing a child's record of every game they have played is worth one
        // confirmation.
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = { Text(stringResource(R.string.stats_reset_confirm_title)) },
            text = { Text(stringResource(R.string.stats_reset_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetStatistics()
                        confirmingReset = false
                    },
                ) {
                    Text(stringResource(R.string.stats_reset_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingReset = false }) {
                    Text(stringResource(R.string.stats_reset_confirm_no))
                }
            },
        )
    }
}

@Composable
private fun StatisticRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CountryQuartetTheme {
        SettingsScreen(onBack = {})
    }
}
