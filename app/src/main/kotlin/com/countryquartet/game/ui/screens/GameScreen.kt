package com.countryquartet.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countryquartet.game.R
import com.countryquartet.game.ui.components.CardState
import com.countryquartet.game.ui.components.CompactCountryCard
import com.countryquartet.game.ui.components.CompletedQuartetCard
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.CountryQuartetTheme
import com.countryquartet.game.viewmodel.GameMessage
import com.countryquartet.game.viewmodel.GameUiState
import com.countryquartet.game.viewmodel.GameViewModel
import com.countryquartet.game.viewmodel.PlayerStanding
import com.countryquartet.game.viewmodel.QuartetGroup

@Composable
fun GameScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(LocalContext.current)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = stringResource(R.string.title_game),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        when (val state = uiState) {
            GameUiState.Loading -> CenteredMessage(stringResource(R.string.game_loading), innerPadding)
            is GameUiState.Failed -> CenteredMessage(
                stringResource(R.string.game_failed, state.message),
                innerPadding,
            )
            is GameUiState.Playing -> GameContent(
                state = state,
                innerPadding = innerPadding,
                onQuartetClick = viewModel::selectQuartet,
                onCountryClick = viewModel::selectCountry,
                onOpponentClick = viewModel::selectOpponent,
                onAsk = viewModel::ask,
                onPlayAgain = viewModel::startNewGame,
                onMainMenu = onBack,
            )
        }
    }
}

@Composable
private fun CenteredMessage(text: String, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun GameContent(
    state: GameUiState.Playing,
    innerPadding: PaddingValues,
    onQuartetClick: (String) -> Unit,
    onCountryClick: (String) -> Unit,
    onOpponentClick: (String) -> Unit,
    onAsk: () -> Unit,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScoreBoard(state.standings)
        StatusLine(state)

        if (state.isFinished) {
            GameOverPanel(
                state = state,
                onPlayAgain = onPlayAgain,
                onMainMenu = onMainMenu,
                modifier = Modifier.weight(1f),
            )
        } else {
            HandList(
                state = state,
                onQuartetClick = onQuartetClick,
                onCountryClick = onCountryClick,
                modifier = Modifier.weight(1f),
            )
            OpponentPicker(state, onOpponentClick)
            Button(
                onClick = onAsk,
                enabled = state.canAsk,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            ) {
                Text(stringResource(R.string.game_ask))
            }
        }
    }
}

@Composable
private fun ScoreBoard(standings: List<PlayerStanding>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        standings.forEach { player ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (player.isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        fontWeight = if (player.isHuman) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text(text = "${player.score}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(R.string.game_cards_left, player.cardCount),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusLine(state: GameUiState.Playing) {
    val turn = if (state.isHumanTurn) {
        stringResource(R.string.game_turn_yours)
    } else {
        stringResource(R.string.game_turn_other, state.currentPlayerName)
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!state.isFinished) {
            Text(text = turn, style = MaterialTheme.typography.titleMedium)
        }
        state.message?.let { message ->
            Text(
                text = messageText(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(
                R.string.game_progress,
                state.completedQuartetsCount,
                state.totalQuartets,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun messageText(message: GameMessage): String = when (message) {
    // Separate sentences per point of view: the human player is called "You",
    // which does not fit a third person verb.
    is GameMessage.CardReceived -> when {
        message.askerIsHuman -> stringResource(
            R.string.game_msg_received_by_you,
            message.targetName,
            message.countryName,
        )
        message.targetIsHuman -> stringResource(
            R.string.game_msg_received_from_you,
            message.askerName,
            message.countryName,
        )
        else -> stringResource(
            R.string.game_msg_received,
            message.askerName,
            message.targetName,
            message.countryName,
        )
    }
    is GameMessage.CardRefused -> if (message.targetIsHuman) {
        stringResource(R.string.game_msg_refused_by_you, message.countryName)
    } else {
        stringResource(R.string.game_msg_refused, message.targetName, message.countryName)
    }
    is GameMessage.QuartetCompleted -> stringResource(
        R.string.game_msg_quartet,
        message.playerName,
        message.quartetName,
    )
}

@Composable
private fun HandList(
    state: GameUiState.Playing,
    onQuartetClick: (String) -> Unit,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.game_your_cards),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        items(state.hand, key = { it.quartet.id }) { group ->
            QuartetGroupCard(
                group = group,
                isSelected = group.quartet.id == state.selection.quartetId,
                selectedCountryId = state.selection.countryId,
                enabled = state.isHumanTurn,
                onQuartetClick = onQuartetClick,
                onCountryClick = onCountryClick,
            )
        }
        if (state.humanCompletedQuartets.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.game_your_quartets),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(state.humanCompletedQuartets, key = { "done_${it.quartet.id}" }) { entry ->
                CompletedQuartetCard(quartet = entry.quartet, countries = entry.countries)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuartetGroupCard(
    group: QuartetGroup,
    isSelected: Boolean,
    selectedCountryId: String?,
    enabled: Boolean,
    onQuartetClick: (String) -> Unit,
    onCountryClick: (String) -> Unit,
) {
    Card(
        onClick = { onQuartetClick(group.quartet.id) },
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${group.quartet.name}  ${group.owned.size}/4",
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                group.owned.forEach { country ->
                    CompactCountryCard(
                        countryId = country.id,
                        name = country.name,
                        capital = country.capital,
                        state = CardState.Owned,
                    )
                }
            }
            if (isSelected) {
                Text(
                    text = stringResource(R.string.game_hint_pick_country),
                    style = MaterialTheme.typography.labelSmall,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    group.missing.forEach { country ->
                        CompactCountryCard(
                            countryId = country.id,
                            name = country.name,
                            capital = null,
                            state = when {
                                !enabled -> CardState.Disabled
                                country.id == selectedCountryId -> CardState.Selected
                                else -> CardState.Normal
                            },
                            onClick = { onCountryClick(country.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpponentPicker(state: GameUiState.Playing, onOpponentClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(
                if (state.selection.quartetId == null) {
                    R.string.game_hint_pick_group
                } else {
                    R.string.game_hint_pick_opponent
                },
            ),
            style = MaterialTheme.typography.labelMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.opponents.forEach { opponent ->
                FilterChip(
                    selected = opponent.id == state.selection.opponentId,
                    onClick = { onOpponentClick(opponent.id) },
                    enabled = state.isHumanTurn,
                    modifier = Modifier.weight(1f),
                    label = { Text(opponent.name, maxLines = 1) },
                )
            }
        }
    }
}

@Composable
private fun GameOverPanel(
    state: GameUiState.Playing,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = stringResource(R.string.game_over), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = if (state.isDraw) {
                stringResource(R.string.game_draw, state.winnerNames.joinToString())
            } else {
                stringResource(R.string.game_winner, state.winnerNames.joinToString())
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        state.standings.sortedByDescending { it.score }.forEach { player ->
            Text(
                text = stringResource(R.string.game_score_line, player.name, player.score),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (player.isWinner) FontWeight.Bold else FontWeight.Normal,
            )
        }
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.game_play_again))
        }
        OutlinedButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.game_main_menu))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    CountryQuartetTheme {
        CenteredMessage(stringResource(R.string.game_loading), PaddingValues(0.dp))
    }
}
