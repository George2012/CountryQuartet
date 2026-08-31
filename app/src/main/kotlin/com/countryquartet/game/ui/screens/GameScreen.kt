package com.countryquartet.game.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.countryquartet.game.ui.components.DeckPile
import com.countryquartet.game.ui.components.LocalAnimationsEnabled
import com.countryquartet.game.ui.components.Motion
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.CountryQuartetTheme
import com.countryquartet.game.ui.theme.quartetBackground
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
            is GameUiState.Playing -> CompositionLocalProvider(
                LocalAnimationsEnabled provides state.animationsEnabled,
            ) {
                GameContent(
                    state = state,
                    innerPadding = innerPadding,
                    onQuartetClick = viewModel::selectQuartet,
                    onCountryClick = viewModel::selectCountry,
                    onOpponentClick = viewModel::selectOpponent,
                    onAskRegion = viewModel::askRegion,
                    onAsk = viewModel::ask,
                    onPlayAgain = viewModel::startNewGame,
                    onMainMenu = onBack,
                )
            }
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
    onAskRegion: () -> Unit,
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
        QuartetCompletedBanner(state.justCompletedQuartet)

        if (state.isFinished) {
            AnimatedVisibility(
                visible = true,
                enter = if (LocalAnimationsEnabled.current) {
                    fadeIn(tween(Motion.ENTER_MS)) +
                        scaleIn(tween(Motion.ENTER_MS), initialScale = 0.9f)
                } else {
                    EnterTransition.None
                },
                modifier = Modifier.weight(1f),
            ) {
                GameOverPanel(
                    state = state,
                    onPlayAgain = onPlayAgain,
                    onMainMenu = onMainMenu,
                )
            }
        } else {
            HandList(
                state = state,
                onQuartetClick = onQuartetClick,
                onCountryClick = onCountryClick,
                onAskRegion = onAskRegion,
                onAsk = onAsk,
                modifier = Modifier.weight(1f),
            )
            OpponentPicker(state, onOpponentClick)
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
            // Turn changes are shown by the highlight moving along the score
            // board rather than by anything jumping.
            val containerColor by animateColorAsState(
                targetValue = if (player.isCurrent) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                animationSpec = Motion.spec(),
                label = "turnHighlight",
            )
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = containerColor),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusText(turn = turn, state = state, modifier = Modifier.weight(1f))
        DeckPile(count = state.deckCount)
    }
}

@Composable
private fun StatusText(turn: String, state: GameUiState.Playing, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (!state.isFinished) {
            Text(text = turn, style = MaterialTheme.typography.titleMedium)
        }
        // The message is the only place a player learns what happened, so it
        // changes with a short cross fade and is tinted by the outcome.
        val animate = LocalAnimationsEnabled.current
        AnimatedContent(
            targetState = state.message,
            transitionSpec = {
                if (animate) {
                    fadeIn(tween(Motion.ENTER_MS)) togetherWith fadeOut(tween(Motion.QUICK_MS))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "gameMessage",
        ) { message ->
            if (message != null) {
                Text(
                    text = messageText(message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (message) {
                        is GameMessage.CardRefused -> MaterialTheme.colorScheme.error
                        is GameMessage.RegionAbsent -> MaterialTheme.colorScheme.error
                        is GameMessage.QuartetCompleted -> MaterialTheme.colorScheme.primary
                        is GameMessage.CardReceived -> MaterialTheme.colorScheme.onSurfaceVariant
                        is GameMessage.RegionPresent -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
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
    is GameMessage.RegionPresent -> if (message.targetIsHuman) {
        stringResource(R.string.game_msg_region_present_by_you, message.quartetName)
    } else {
        stringResource(R.string.game_msg_region_present, message.targetName, message.quartetName)
    }
    is GameMessage.RegionAbsent -> if (message.targetIsHuman) {
        stringResource(R.string.game_msg_region_absent_by_you, message.quartetName)
    } else {
        stringResource(R.string.game_msg_region_absent, message.targetName, message.quartetName)
    }
    is GameMessage.QuartetCompleted -> stringResource(
        R.string.game_msg_quartet,
        message.playerName,
        message.quartet.name,
    )
}

@Composable
private fun HandList(
    state: GameUiState.Playing,
    onQuartetClick: (String) -> Unit,
    onCountryClick: (String) -> Unit,
    onAskRegion: () -> Unit,
    onAsk: () -> Unit,
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
            val isSelected = group.quartet.id == state.selection.quartetId
            QuartetGroupCard(
                modifier = if (LocalAnimationsEnabled.current) Modifier.animateItem() else Modifier,
                group = group,
                isSelected = isSelected,
                selectedCountryId = state.selection.countryId,
                regionConfirmed = state.isRegionConfirmed(group.quartet.id),
                enabled = state.isHumanTurn,
                // The ask buttons only ever apply to the selected card, so the
                // rest of the hand does not need to care whether asking is
                // currently possible.
                canAskRegion = isSelected && state.canAskRegion,
                canAsk = isSelected && state.canAsk,
                onQuartetClick = onQuartetClick,
                onCountryClick = onCountryClick,
                onAskRegion = onAskRegion,
                onAsk = onAsk,
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
    modifier: Modifier = Modifier,
    group: QuartetGroup,
    isSelected: Boolean,
    selectedCountryId: String?,
    regionConfirmed: Boolean,
    enabled: Boolean,
    canAskRegion: Boolean,
    canAsk: Boolean,
    onQuartetClick: (String) -> Unit,
    onCountryClick: (String) -> Unit,
    onAskRegion: () -> Unit,
    onAsk: () -> Unit,
) {
    // The background now identifies the region, so selection cannot be shown by
    // swapping the colour: it is an outline and a lift instead.
    val regionColor by animateColorAsState(
        targetValue = quartetBackground(group.quartet.id),
        animationSpec = Motion.spec(),
        label = "regionBackground",
    )
    val outline by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = Motion.spec(),
        label = "regionOutline",
    )
    Card(
        onClick = { onQuartetClick(group.quartet.id) },
        enabled = enabled,
        modifier = modifier.then(
            if (outline > 0.dp) {
                Modifier.border(outline, MaterialTheme.colorScheme.primary, CardDefaults.shape)
            } else {
                Modifier
            },
        ),
        colors = CardDefaults.cardColors(containerColor = regionColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp,
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
                if (regionConfirmed) {
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
                                // Cards you do not hold yet show their capital too:
                                // it keeps every card the same height and there is
                                // something to learn while choosing.
                                capital = country.capital,
                                state = when {
                                    !enabled -> CardState.Disabled
                                    country.id == selectedCountryId -> CardState.Selected
                                    else -> CardState.Normal
                                },
                                onClick = { onCountryClick(country.id) },
                            )
                        }
                    }
                    Button(
                        onClick = onAsk,
                        enabled = canAsk,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.game_ask))
                    }
                } else {
                    // The specific countries stay hidden - and unaskable -
                    // until the asked player has confirmed they hold the
                    // group at all. Picking a player is done below; this
                    // button only lights up once one is picked.
                    Button(
                        onClick = onAskRegion,
                        enabled = canAskRegion,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.game_ask_region))
                    }
                }
            }
        }
    }
}

@Composable
private fun OpponentPicker(state: GameUiState.Playing, onOpponentClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val hint = when {
            state.selection.quartetId == null -> R.string.game_hint_pick_group
            state.selection.opponentId == null -> R.string.game_hint_pick_opponent
            !state.isRegionConfirmed(state.selection.quartetId) -> R.string.game_hint_ask_region
            else -> R.string.game_hint_pick_country
        }
        Text(
            text = stringResource(hint),
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

/**
 * The "QUARTET COMPLETED!" moment: the finished set is shown with all four
 * countries for a moment and then gets out of the way again.
 */
@Composable
private fun QuartetCompletedBanner(completed: GameMessage.QuartetCompleted?) {
    val animate = LocalAnimationsEnabled.current
    var shown by remember { mutableStateOf<GameMessage.QuartetCompleted?>(null) }

    LaunchedEffect(completed) {
        shown = completed
        if (completed != null) {
            kotlinx.coroutines.delay(Motion.BANNER_MS)
            shown = null
        }
    }

    AnimatedVisibility(
        visible = shown != null,
        enter = if (animate) {
            fadeIn(tween(Motion.ENTER_MS)) + scaleIn(tween(Motion.ENTER_MS), initialScale = 0.85f)
        } else {
            EnterTransition.None
        },
        exit = if (animate) {
            fadeOut(tween(Motion.QUICK_MS)) + scaleOut(tween(Motion.QUICK_MS), targetScale = 0.95f)
        } else {
            ExitTransition.None
        },
    ) {
        shown?.let { banner ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.game_quartet_banner, banner.playerName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                CompletedQuartetCard(quartet = banner.quartet, countries = banner.countries)
            }
        }
    }
}
