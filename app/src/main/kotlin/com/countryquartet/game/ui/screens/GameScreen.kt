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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countryquartet.game.R
import com.countryquartet.game.ui.components.CardState
import com.countryquartet.game.ui.components.CompactCountryCard
import com.countryquartet.game.ui.components.CompletedQuartetCard
import com.countryquartet.game.ui.components.DeckPile
import com.countryquartet.game.ui.components.LocalAnimationsEnabled
import com.countryquartet.game.ui.components.Motion
import com.countryquartet.game.ui.components.PlayerAvatar
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.CountryQuartetTheme
import com.countryquartet.game.ui.theme.goodColor
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
                    onNext = viewModel::advance,
                    onToggleAuto = viewModel::setAutoPlay,
                    onTakeCard = viewModel::takeCard,
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
    onNext: () -> Unit,
    onToggleAuto: (Boolean) -> Unit,
    onTakeCard: () -> Unit,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
) {
    var showHistory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScoreBoard(
            standings = state.standings,
            selectedOpponentId = state.selection.opponentId,
            canChoose = state.canAct,
            onOpponentClick = onOpponentClick,
        )
        StatusLine(state, onHistoryClick = { showHistory = true }, onTakeCard = onTakeCard)
        TurnControls(state = state, onNext = onNext, onToggleAuto = onToggleAuto)
        TakenCardBanner(state.justTookCard)
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
        }
    }

    if (showHistory) {
        HistoryDialog(history = state.history, onDismiss = { showHistory = false })
    }
}

@Composable
private fun ScoreBoard(
    standings: List<PlayerStanding>,
    selectedOpponentId: String?,
    canChoose: Boolean,
    onOpponentClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        standings.forEachIndexed { seatIndex, player ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                // Turn changes are shown by the highlight moving along the
                // score board rather than by anything jumping.
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayerAvatar(
                            isHuman = player.isHuman,
                            playerId = player.id,
                            seatIndex = seatIndex,
                            isCurrent = player.isCurrent,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
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
                // Only opponents can be asked - the human cannot pick themselves.
                if (!player.isHuman) {
                    FilterChip(
                        selected = player.id == selectedOpponentId,
                        onClick = { onOpponentClick(player.id) },
                        enabled = canChoose,
                        modifier = Modifier.padding(top = 4.dp),
                        label = { Text(stringResource(R.string.game_choose_opponent), maxLines = 1) },
                    )
                }
            }
        }
    }
}

/**
 * Who has to do something next, and how the computer players are paced.
 *
 * A game is stepped by default: the computer players wait on Next, one ask per
 * press, so their turns can be followed. AUTO hands the pacing back to the
 * timer and disables Next.
 */
@Composable
private fun TurnControls(
    state: GameUiState.Playing,
    onNext: () -> Unit,
    onToggleAuto: (Boolean) -> Unit,
) {
    if (state.isFinished) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isHumanTurn) {
            // While a card is owed the banner says so instead, because taking
            // it is the only thing the Take button will let happen next.
            YourTurnBanner(
                text = if (state.mustTakeCard) {
                    stringResource(R.string.game_take_banner)
                } else {
                    stringResource(R.string.game_your_turn_banner)
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            Button(
                onClick = onNext,
                enabled = state.canStep && !state.autoPlay,
                modifier = Modifier.weight(1f),
            ) {
                // While auto play runs there is nothing to press, so the button
                // says why rather than sitting there greyed out and unexplained.
                Text(
                    text = if (state.autoPlay) {
                        stringResource(R.string.game_auto_hint)
                    } else {
                        stringResource(R.string.game_next)
                    },
                    maxLines = 1,
                )
            }
        }
        FilterChip(
            selected = state.autoPlay,
            onClick = { onToggleAuto(!state.autoPlay) },
            label = { Text(stringResource(R.string.game_auto), maxLines = 1) },
        )
    }
}

/** Takes the place of the Next button, so the turn never looks stuck. */
@Composable
private fun YourTurnBanner(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
        )
    }
}

@Composable
private fun StatusLine(
    state: GameUiState.Playing,
    onHistoryClick: () -> Unit,
    onTakeCard: () -> Unit,
) {
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
        StatusText(
            turn = turn,
            state = state,
            onHistoryClick = onHistoryClick,
            modifier = Modifier.weight(1f),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DeckPile(count = state.deckCount)
            // The card a lost turn pays out is taken off this pile, so the
            // button that takes it belongs right under it.
            if (state.mustTakeCard) {
                Button(
                    onClick = onTakeCard,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(stringResource(R.string.game_take), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun StatusText(
    turn: String,
    state: GameUiState.Playing,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (!state.isFinished) {
            Text(text = turn, style = MaterialTheme.typography.titleMedium)
            // An outline and a tonal fill: as plain text this was easy to read
            // straight past, and nothing else on the screen says it opens
            // anything.
            OutlinedButton(
                onClick = onHistoryClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.heightIn(min = 36.dp).padding(vertical = 2.dp),
            ) {
                Text(stringResource(R.string.game_history), style = MaterialTheme.typography.labelMedium)
            }
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
                    color = messageColor(message),
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

/** The colour a message is shown in, wherever it appears - the status line or the history list. */
@Composable
private fun messageColor(message: GameMessage): Color =
    if (message.isGoodForHuman()) goodColor() else MaterialTheme.colorScheme.error

/**
 * Did this go the human player's way?
 *
 * Judged from the human's seat rather than the acting player's, because that
 * is the seat reading the message: a computer losing its turn is good news
 * even though nothing of the player's moved, and a computer winning a card off
 * another computer is bad news for the same reason.
 */
internal fun GameMessage.isGoodForHuman(): Boolean = when (this) {
    // An ask that worked is good for whoever asked, so it is good news here
    // only when the human was the one asking.
    is GameMessage.CardReceived -> askerIsHuman
    is GameMessage.RegionPresent -> askerIsHuman
    is GameMessage.QuartetCompleted -> askerIsHuman
    // An ask that failed ends the asker's turn, which is good for everyone
    // else at the table - the human included, whoever was asking.
    is GameMessage.CardRefused -> !askerIsHuman
    is GameMessage.RegionAbsent -> !askerIsHuman
    // The card a lost turn pays out. The turn was already lost and reported
    // as such; this message is the card you gained for it.
    is GameMessage.CardTaken -> true
}

/**
 * Every action taken so far this game. Most recent first, one card per
 * record, and scrollable once it grows past the dialog's height.
 */
@Composable
private fun HistoryDialog(history: List<GameMessage>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.game_history_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (history.isEmpty()) {
                    Text(
                        text = stringResource(R.string.game_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp).padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(history.size) { index ->
                            HistoryEntryCard(history[index])
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        }
    }
}

/**
 * One history record as a single element: who asked whom, what they asked
 * about, and how it went.
 */
@Composable
private fun HistoryEntryCard(message: GameMessage) {
    val (asked, about, result) = historyLines(message)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = asked, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = about,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = result,
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor(message),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** The three lines of a history record: who asked whom, what, and the result. */
@Composable
private fun historyLines(message: GameMessage): Triple<String, String, String> = when (message) {
    is GameMessage.CardReceived -> Triple(
        askedLine(message.askerName, message.targetName, message.askerIsHuman, message.targetIsHuman),
        stringResource(R.string.history_about_country, message.countryName),
        stringResource(R.string.history_result_got_it),
    )
    is GameMessage.CardRefused -> Triple(
        askedLine(message.askerName, message.targetName, message.askerIsHuman, message.targetIsHuman),
        stringResource(R.string.history_about_country, message.countryName),
        stringResource(R.string.history_result_no),
    )
    is GameMessage.RegionPresent -> Triple(
        askedLine(message.askerName, message.targetName, message.askerIsHuman, message.targetIsHuman),
        stringResource(R.string.history_about_region, message.quartetName),
        stringResource(R.string.history_result_yes),
    )
    is GameMessage.RegionAbsent -> Triple(
        askedLine(message.askerName, message.targetName, message.askerIsHuman, message.targetIsHuman),
        stringResource(R.string.history_about_region, message.quartetName),
        stringResource(R.string.history_result_no),
    )
    is GameMessage.QuartetCompleted -> Triple(
        askedLine(message.playerName, message.targetName, message.askerIsHuman, message.targetIsHuman),
        stringResource(R.string.history_about_country, message.countryName),
        stringResource(R.string.history_result_quartet, message.quartet.name),
    )
    // Nobody was asked for this one, so the middle line names where it came
    // from instead of what was asked about.
    is GameMessage.CardTaken -> Triple(
        stringResource(R.string.history_took),
        stringResource(R.string.history_from_deck),
        message.country.name,
    )
}

/** "You asked X" / "X asked you" / "X asked Y" - whichever side of the ask is human, if either. */
@Composable
private fun askedLine(
    askerName: String,
    targetName: String,
    askerIsHuman: Boolean,
    targetIsHuman: Boolean,
): String = when {
    askerIsHuman -> stringResource(R.string.history_asked_by_you, targetName)
    targetIsHuman -> stringResource(R.string.history_asked_you, askerName)
    else -> stringResource(R.string.history_asked, askerName, targetName)
}

@Composable
private fun messageText(message: GameMessage): String = when (message) {
    // Who asked whom, and for what. Separate sentences per point of view: the
    // human player is called "You", which does not fit a third person verb.
    is GameMessage.CardReceived -> when {
        message.askerIsHuman -> stringResource(
            R.string.game_msg_received_you_asked,
            message.targetName,
            message.countryName,
        )
        message.targetIsHuman -> stringResource(
            R.string.game_msg_received_asked_you,
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
    is GameMessage.CardRefused -> when {
        message.askerIsHuman -> stringResource(
            R.string.game_msg_refused_you_asked,
            message.targetName,
            message.countryName,
        )
        message.targetIsHuman -> stringResource(
            R.string.game_msg_refused_asked_you,
            message.askerName,
            message.countryName,
        )
        else -> stringResource(
            R.string.game_msg_refused,
            message.askerName,
            message.targetName,
            message.countryName,
        )
    }
    is GameMessage.RegionPresent -> when {
        message.askerIsHuman -> stringResource(
            R.string.game_msg_region_present_you_asked,
            message.targetName,
            message.quartetName,
        )
        message.targetIsHuman -> stringResource(
            R.string.game_msg_region_present_asked_you,
            message.askerName,
            message.quartetName,
        )
        else -> stringResource(
            R.string.game_msg_region_present,
            message.askerName,
            message.targetName,
            message.quartetName,
        )
    }
    is GameMessage.RegionAbsent -> when {
        message.askerIsHuman -> stringResource(
            R.string.game_msg_region_absent_you_asked,
            message.targetName,
            message.quartetName,
        )
        message.targetIsHuman -> stringResource(
            R.string.game_msg_region_absent_asked_you,
            message.askerName,
            message.quartetName,
        )
        else -> stringResource(
            R.string.game_msg_region_absent,
            message.askerName,
            message.targetName,
            message.quartetName,
        )
    }
    is GameMessage.QuartetCompleted -> when {
        message.askerIsHuman -> stringResource(
            R.string.game_msg_quartet_you_asked,
            message.targetName,
            message.countryName,
            message.quartet.name,
        )
        message.targetIsHuman -> stringResource(
            R.string.game_msg_quartet_asked_you,
            message.playerName,
            message.countryName,
            message.quartet.name,
        )
        else -> stringResource(
            R.string.game_msg_quartet,
            message.playerName,
            message.targetName,
            message.countryName,
            message.quartet.name,
        )
    }
    is GameMessage.CardTaken -> stringResource(R.string.game_msg_took, message.country.name)
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
                enabled = state.canAct,
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
                        quartetId = group.quartet.id,
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
                                quartetId = group.quartet.id,
                                name = country.name,
                                // Cards you do not hold yet show their capital too:
                                // it keeps every card the same height and there is
                                // something to learn while choosing.
                                capital = country.capital,
                                // Smaller than the owned row above: there can be up
                                // to three of these, and this row is only ever
                                // picked from, never referred back to.
                                small = true,
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
 * The card just taken off the pile, held up for a moment.
 *
 * A card that only appeared somewhere in a hand sorted by quartet would leave
 * the player hunting for what was new, so the card itself is shown.
 */
@Composable
private fun TakenCardBanner(taken: GameMessage.CardTaken?) {
    val animate = LocalAnimationsEnabled.current
    var shown by remember { mutableStateOf<GameMessage.CardTaken?>(null) }

    LaunchedEffect(taken) {
        shown = taken
        if (taken != null) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.game_took_banner),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                CompactCountryCard(
                    countryId = banner.country.id,
                    quartetId = banner.country.quartetId,
                    name = banner.country.name,
                    capital = banner.country.capital,
                    state = CardState.Selected,
                )
            }
        }
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
