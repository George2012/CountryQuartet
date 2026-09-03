package com.countryquartet.game.viewmodel

import com.countryquartet.game.data.AssetFiles
import com.countryquartet.game.game.GameEngine
import com.countryquartet.game.data.GameDataSource
import com.countryquartet.game.ai.BasicAi
import com.countryquartet.game.model.GameSettings
import com.countryquartet.game.repository.CountryRepository
import com.countryquartet.game.repository.InMemorySettingsRepository
import com.countryquartet.game.repository.InMemoryStatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Drives the ViewModel the way the screen does: pick a group, pick a country,
 * pick an opponent, ask - and let the computer turns run on virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    /**
     * A game set up the way most of these tests want it: on auto play, so the
     * computer turns run by themselves and `advanceUntilIdle` is enough to get
     * back to the human. Pass `autoPlay = false` to drive them by hand.
     */
    private fun newViewModel(
        seed: Long = 1,
        animations: Boolean = true,
        autoPlay: Boolean = true,
    ) = GameViewModel(
        repository = CountryRepository(AssetFiles),
        ai = BasicAi(Random(seed)),
        random = Random(seed),
        settings = InMemorySettingsRepository(GameSettings(animationsEnabled = animations)),
    ).apply { setAutoPlay(autoPlay) }

    private val GameViewModel.playing: GameUiState.Playing
        get() = uiState.value as GameUiState.Playing

    @Test
    fun `a new game deals a hand and starts with the human`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.playing
        assertEquals(4, state.standings.size)
        assertTrue(state.isHumanTurn)
        assertTrue(state.hand.isNotEmpty())
        // Every player holds the dealt hand size, minus any quartet that was
        // dealt complete and laid down straight away.
        state.standings.forEach { player ->
            assertEquals(
                player.name,
                GameEngine.DEFAULT_CARDS_PER_PLAYER,
                player.cardCount + player.score * 4,
            )
        }
        assertEquals(52 - 4 * GameEngine.DEFAULT_CARDS_PER_PLAYER, state.deckCount)
        assertEquals(state.standings.sumOf { it.score }, state.completedQuartetsCount)
    }

    @Test
    fun `broken content is reported instead of crashing`() = runTest {
        val broken = object : GameDataSource {
            override fun readCountriesJson(): String = "[]"
            override fun readQuartetsJson(): String = AssetFiles.readQuartetsJson()
        }

        val viewModel = GameViewModel(CountryRepository(broken))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is GameUiState.Failed)
    }

    @Test
    fun `selecting a group and opponent enables asking the region`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        assertEquals(group.quartet.id, viewModel.playing.selection.quartetId)
        assertFalse(viewModel.playing.canAskRegion)
        assertFalse(viewModel.playing.canAsk)

        viewModel.selectOpponent(viewModel.playing.opponents.first().id)

        assertTrue(viewModel.playing.canAskRegion)
        // The specific country cannot be asked until the region comes back present.
        assertFalse(viewModel.playing.canAsk)
    }

    @Test
    fun `a present region unlocks asking the specific country`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        viewModel.selectOpponent(viewModel.playing.opponents.first().id)
        viewModel.askRegion()

        if (viewModel.playing.isRegionConfirmed(group.quartet.id)) {
            assertFalse(viewModel.playing.canAskRegion)
            viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
            assertTrue(viewModel.playing.canAsk)
        } else {
            // The region was absent: the turn moved on and the pick was cleared.
            assertEquals(null, viewModel.playing.selection.quartetId)
        }
    }

    @Test
    fun `asking the specific country without confirming the region first does nothing`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()
        val before = viewModel.playing

        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        viewModel.selectCountry(group.missing.first().id)
        viewModel.selectOpponent(viewModel.playing.opponents.first().id)
        assertTrue(viewModel.playing.canAskRegion)
        assertFalse(viewModel.playing.canAsk)

        viewModel.ask()
        advanceUntilIdle()

        assertEquals(before.standings, viewModel.playing.standings)
        assertEquals(null, viewModel.playing.message)
    }

    @Test
    fun `asking the region never transfers a card - a present region changes nothing, an absent one only draws a consolation card`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()
        val ownedBefore = viewModel.playing.hand.sumOf { it.owned.size }

        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        viewModel.selectOpponent(viewModel.playing.opponents.first().id)
        viewModel.askRegion()

        val ownedAfter = viewModel.playing.hand.sumOf { it.owned.size }
        if (viewModel.playing.isRegionConfirmed(group.quartet.id)) {
            assertEquals(ownedBefore, ownedAfter)
        } else {
            // Absent: the human drew a consolation card, exactly like a
            // failed specific-card request would.
            assertEquals(ownedBefore + 1, ownedAfter)
        }
        assertNotNull(viewModel.playing.message)
    }

    @Test
    fun `tapping the same group again clears the selection`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val quartetId = viewModel.playing.hand.first().quartet.id
        viewModel.selectQuartet(quartetId)
        viewModel.selectQuartet(quartetId)

        assertEquals(null, viewModel.playing.selection.quartetId)
    }

    @Test
    fun `asking without a full selection does nothing`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()
        val before = viewModel.playing

        viewModel.ask()
        advanceUntilIdle()

        assertEquals(before.standings, viewModel.playing.standings)
        assertEquals(null, viewModel.playing.message)
    }

    @Test
    fun `a human can play a complete game to the end`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        var humanMoves = 0
        while (!viewModel.playing.isFinished) {
            check(humanMoves++ < 500) { "the game did not finish" }
            val state = viewModel.playing
            assertTrue("waiting for the human but it is not their turn", state.isHumanTurn)

            val group = state.hand.first()
            if (state.selection.quartetId != group.quartet.id) {
                viewModel.selectQuartet(group.quartet.id)
            }
            // Selecting is a toggle, and a successful ask keeps the opponent
            // picked, so tapping the same one again would clear it.
            val opponent = state.opponents.first().id
            if (viewModel.playing.selection.opponentId != opponent) {
                viewModel.selectOpponent(opponent)
            }

            if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) {
                viewModel.askRegion()
                advanceUntilIdle()
                // A "no" ends the turn - and by the time the computer players
                // are done, play may already be back with the human on a
                // fresh selection, so check confirmation rather than whose
                // turn it is.
                if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) continue
            }

            val selected = requireNotNull(viewModel.playing.selectedGroup) {
                "group ${group.quartet.id} should be selected"
            }
            viewModel.selectCountry(selected.missing.first().id)
            assertTrue("nothing to ask with", viewModel.playing.canAsk)

            viewModel.ask()
            // Let the computer players finish their turns.
            advanceUntilIdle()
        }

        val finished = viewModel.playing
        assertEquals(13, finished.completedQuartetsCount)
        assertEquals(13, finished.standings.sumOf { it.score })
        assertTrue(finished.winnerNames.isNotEmpty())
        assertNotNull(finished.message)
    }

    @Test
    fun `messages record who asked and who was asked`() = runTest {
        val viewModel = newViewModel(seed = 4)
        advanceUntilIdle()
        val humanName = viewModel.playing.human.name
        val seen = mutableListOf<GameMessage>()

        var moves = 0
        while (!viewModel.playing.isFinished && moves++ < 800) {
            val state = viewModel.playing
            val group = state.hand.first()
            if (state.selection.quartetId != group.quartet.id) {
                viewModel.selectQuartet(group.quartet.id)
            }
            // Rotate through the opponents; always asking the same one can go a
            // whole game without a single hit. Skip when already selected,
            // because selecting is a toggle.
            val opponent = state.opponents[moves % state.opponents.size].id
            if (viewModel.playing.selection.opponentId != opponent) {
                viewModel.selectOpponent(opponent)
            }

            if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) {
                viewModel.askRegion()
                viewModel.playing.message?.let { seen += it }
                advanceUntilIdle()
                viewModel.playing.message?.let { seen += it }
                // A "no" ends the turn - and by the time the computer players
                // are done, play may already be back with the human on a
                // fresh selection, so check confirmation rather than whose
                // turn it is.
                if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) continue
            }

            viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
            viewModel.ask()
            // Sampled before the computer turns run, so this is the human move.
            // A StateFlow conflates, so collecting it would drop messages.
            viewModel.playing.message?.let { seen += it }

            advanceUntilIdle()
            viewModel.playing.message?.let { seen += it }
        }

        // The flags must always agree with the names, otherwise the screen
        // would pick a sentence written for the wrong player.
        seen.filterIsInstance<GameMessage.CardRefused>().forEach { message ->
            assertEquals(message.toString(), message.targetIsHuman, message.targetName == humanName)
        }
        seen.filterIsInstance<GameMessage.CardReceived>().forEach { message ->
            assertEquals(message.toString(), message.askerIsHuman, message.askerName == humanName)
            assertEquals(message.toString(), message.targetIsHuman, message.targetName == humanName)
        }
        // Both points of view have to occur, or the assertions above prove nothing.
        assertTrue(
            "no computer player ever asked the human",
            seen.filterIsInstance<GameMessage.CardRefused>().any { it.targetIsHuman },
        )
        assertTrue(
            "the human never received a card",
            seen.filterIsInstance<GameMessage.CardReceived>().any { it.askerIsHuman },
        )
    }

    @Test
    fun `computer turns pause while animations are on`() = runTest {
        val viewModel = newViewModel(seed = 6, animations = true)
        advanceUntilIdle()

        playHumanTurns(viewModel, count = 6)

        assertTrue(
            "virtual time was ${currentTime}ms",
            currentTime >= GameViewModel.AI_TURN_DELAY_MS,
        )
    }

    @Test
    fun `switching animations off removes the thinking pause`() = runTest {
        val viewModel = newViewModel(seed = 6, animations = false)
        advanceUntilIdle()

        playHumanTurns(viewModel, count = 6)

        assertEquals(0L, currentTime)
        // The game still runs: turns were taken, they just did not wait.
        assertTrue(viewModel.playing.message != null)
    }

    @Test
    fun `the animation setting reaches a game that is already running`() = runTest {
        val settings = InMemorySettingsRepository()
        val viewModel = GameViewModel(
            repository = CountryRepository(AssetFiles),
            ai = BasicAi(Random(6)),
            random = Random(6),
            settings = settings,
        )
        advanceUntilIdle()
        assertTrue(viewModel.playing.animationsEnabled)

        settings.setAnimationsEnabled(false)
        advanceUntilIdle()

        assertFalse(viewModel.playing.animationsEnabled)
    }

    /**
     * Plays [count] human turns, asking about the region first whenever it is
     * not already confirmed, and letting the computer players finish each time.
     */
    private fun TestScope.playHumanTurns(
        viewModel: GameViewModel,
        count: Int,
    ) {
        repeat(count) { move ->
            if (viewModel.playing.isFinished) return
            val state = viewModel.playing
            val group = state.hand.first()
            if (state.selection.quartetId != group.quartet.id) {
                viewModel.selectQuartet(group.quartet.id)
            }
            val opponent = state.opponents[move % state.opponents.size].id
            if (viewModel.playing.selection.opponentId != opponent) {
                viewModel.selectOpponent(opponent)
            }

            if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) {
                viewModel.askRegion()
                advanceUntilIdle()
                // A "no" ends the turn - and by the time the computer players
                // are done, play may already be back with the human on a
                // fresh selection, so check confirmation rather than whose
                // turn it is.
                if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) return@repeat
            }

            viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
            viewModel.ask()
            advanceUntilIdle()
        }
    }

    @Test
    fun `finishing a game is recorded exactly once`() = runTest {
        val statistics = InMemoryStatisticsRepository()
        val settings = InMemorySettingsRepository(GameSettings(animationsEnabled = false))
        val viewModel = GameViewModel(
            repository = CountryRepository(AssetFiles),
            ai = BasicAi(Random(3)),
            random = Random(3),
            settings = settings,
            statistics = statistics,
        ).apply { setAutoPlay(true) }
        advanceUntilIdle()
        assertEquals(0, statistics.statistics.value.gamesPlayed)

        playHumanTurns(viewModel, count = 500)
        assertTrue("the game did not finish", viewModel.playing.isFinished)

        val human = viewModel.playing.human
        assertEquals(1, statistics.statistics.value.gamesPlayed)
        assertEquals(human.score, statistics.statistics.value.totalQuartets)
        assertEquals(human.score, statistics.statistics.value.bestScore)

        // The finished state is published again on every settings change. That
        // must not count the same game a second time.
        repeat(3) { index ->
            settings.setAnimationsEnabled(index % 2 == 0)
            advanceUntilIdle()
        }
        assertTrue("still the same finished game", viewModel.playing.isFinished)
        assertEquals(1, statistics.statistics.value.gamesPlayed)
        assertEquals(human.score, statistics.statistics.value.totalQuartets)
    }

    @Test
    fun `the recorded outcome matches the final standings`() = runTest {
        val statistics = InMemoryStatisticsRepository()
        val viewModel = GameViewModel(
            repository = CountryRepository(AssetFiles),
            ai = BasicAi(Random(9)),
            random = Random(9),
            settings = InMemorySettingsRepository(GameSettings(animationsEnabled = false)),
            statistics = statistics,
        ).apply { setAutoPlay(true) }
        advanceUntilIdle()
        playHumanTurns(viewModel, count = 500)

        val finished = viewModel.playing
        val record = statistics.statistics.value
        val humanWon = finished.human.name in finished.winnerNames
        when {
            humanWon && finished.isDraw -> assertEquals(1, record.draws)
            humanWon -> assertEquals(1, record.gamesWon)
            else -> assertEquals(1, record.gamesLost)
        }
        assertEquals(1, record.gamesPlayed)
    }

    @Test
    fun `play again deals a fresh game`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()
        val first = viewModel.playing.hand.flatMap { it.owned.map { country -> country.id } }

        viewModel.startNewGame()
        advanceUntilIdle()
        val second = viewModel.playing

        assertEquals(0, second.completedQuartetsCount)
        assertEquals(null, second.message)
        assertEquals(null, second.selection.quartetId)
        assertEquals(
            GameEngine.DEFAULT_CARDS_PER_PLAYER,
            second.human.cardCount + second.human.score * 4,
        )
        assertTrue("a reshuffle should not be impossible", first.isNotEmpty())
    }

    @Test
    fun `history accumulates every message, most recent first`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()
        assertEquals(emptyList<GameMessage>(), viewModel.playing.history)

        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        viewModel.selectOpponent(viewModel.playing.opponents.first().id)
        viewModel.askRegion()

        val firstMessage = viewModel.playing.message
        assertNotNull(firstMessage)
        assertEquals(listOf(firstMessage), viewModel.playing.history)

        advanceUntilIdle()
        // Whatever the computer players did next only ever prepends to the
        // history - it never drops what came before.
        val historyAfterAiTurns = viewModel.playing.history
        assertTrue(historyAfterAiTurns.contains(firstMessage))
        assertEquals(viewModel.playing.message, historyAfterAiTurns.first())
    }

    @Test
    fun `play again clears the history`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()
        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        viewModel.selectOpponent(viewModel.playing.opponents.first().id)
        viewModel.askRegion()
        advanceUntilIdle()
        assertTrue(viewModel.playing.history.isNotEmpty())

        viewModel.startNewGame()
        advanceUntilIdle()

        assertEquals(emptyList<GameMessage>(), viewModel.playing.history)
    }

    @Test
    fun `by default the computer players wait to be stepped through`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        assertFalse(viewModel.playing.autoPlay)

        endHumanTurn(viewModel)
        val waiting = viewModel.playing
        assertFalse(waiting.isHumanTurn)
        assertTrue("there should be a computer ask to step through", waiting.canStep)

        // No amount of waiting moves the game on by itself.
        advanceUntilIdle()
        assertEquals(waiting.history, viewModel.playing.history)
        assertEquals(waiting.currentPlayerName, viewModel.playing.currentPlayerName)
    }

    @Test
    fun `one step plays exactly one computer ask`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        endHumanTurn(viewModel)
        val before = viewModel.playing.history.size

        viewModel.advance()
        advanceUntilIdle()

        assertEquals(before + 1, viewModel.playing.history.size)
    }

    @Test
    fun `a computer step asks about the region before it asks for the card`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        endHumanTurn(viewModel)

        viewModel.advance()
        val first = viewModel.playing.message
        assertTrue(
            "$first should be a region question",
            first is GameMessage.RegionPresent || first is GameMessage.RegionAbsent,
        )

        // A "yes" means the same computer player now names a country - on the
        // next press, not straight away.
        if (first is GameMessage.RegionPresent) {
            viewModel.advance()
            val second = viewModel.playing.message
            assertTrue(
                "$second should be about a country",
                second is GameMessage.CardReceived ||
                    second is GameMessage.CardRefused ||
                    second is GameMessage.QuartetCompleted,
            )
        }
    }

    @Test
    fun `turning auto on plays the computer turns without any presses`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        endHumanTurn(viewModel)
        assertTrue(viewModel.playing.canStep)

        viewModel.setAutoPlay(true)
        advanceUntilIdle()

        assertTrue(viewModel.playing.autoPlay)
        assertTrue(
            "play should have come back to the human",
            viewModel.playing.isHumanTurn || viewModel.playing.isFinished,
        )
    }

    @Test
    fun `turning auto off leaves the rest of the turn to the next button`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        endHumanTurn(viewModel)

        viewModel.setAutoPlay(true)
        viewModel.setAutoPlay(false)
        val stopped = viewModel.playing
        advanceUntilIdle()

        assertFalse(viewModel.playing.autoPlay)
        assertEquals(stopped.history, viewModel.playing.history)
    }

    @Test
    fun `there is nothing to step through while it is the human turn`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()

        assertTrue(viewModel.playing.isHumanTurn)
        assertFalse(viewModel.playing.canStep)

        viewModel.advance()
        advanceUntilIdle()

        assertEquals(emptyList<GameMessage>(), viewModel.playing.history)
    }

    @Test
    fun `play again goes back to manual play`() = runTest {
        val viewModel = newViewModel(autoPlay = true)
        advanceUntilIdle()
        assertTrue(viewModel.playing.autoPlay)

        viewModel.startNewGame()
        advanceUntilIdle()

        assertFalse(viewModel.playing.autoPlay)
    }

    /**
     * Plays human moves until the turn passes to a computer player, and stops
     * there: with auto play off that is exactly the position the Next button
     * exists for. A lost turn owes a card, which is taken on the way past.
     */
    private fun endHumanTurn(viewModel: GameViewModel) {
        var guard = 0
        while (viewModel.playing.isHumanTurn && !viewModel.playing.mustTakeCard) {
            check(guard++ < 200) { "the human never lost the turn" }
            playOneHumanAsk(viewModel, guard)
        }
        if (viewModel.playing.mustTakeCard) viewModel.takeCard()
    }

    /** One human ask: the region while it is unconfirmed, then a country. */
    private fun playOneHumanAsk(viewModel: GameViewModel, rotation: Int) {
        val state = viewModel.playing
        val group = state.hand.first()
        if (state.selection.quartetId != group.quartet.id) {
            viewModel.selectQuartet(group.quartet.id)
        }
        // Rotating the opponent keeps a run of lucky guesses from going on
        // forever.
        val opponent = state.opponents[rotation % state.opponents.size].id
        if (viewModel.playing.selection.opponentId != opponent) {
            viewModel.selectOpponent(opponent)
        }

        if (!viewModel.playing.isRegionConfirmed(group.quartet.id)) {
            viewModel.askRegion()
            return
        }
        viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
        viewModel.ask()
    }

    /** Plays human asks until one fails and leaves a card waiting to be taken. */
    private fun playUntilCardOwed(viewModel: GameViewModel) {
        var guard = 0
        while (!viewModel.playing.mustTakeCard) {
            check(guard++ < 200) { "no ask ever failed" }
            check(viewModel.playing.isHumanTurn) { "the turn passed without owing a card" }
            playOneHumanAsk(viewModel, guard)
        }
    }

    @Test
    fun `a lost turn holds the drawn card back until it is taken`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()

        playUntilCardOwed(viewModel)

        val waiting = viewModel.playing
        val cardsBefore = waiting.human.cardCount
        val deckBefore = waiting.deckCount
        // Nothing has moved yet: the card is still on the pile and the turn is
        // not over until it has been taken.
        assertTrue(waiting.isHumanTurn)
        assertFalse("asking again has to be blocked", waiting.canAct)

        viewModel.takeCard()

        val after = viewModel.playing
        assertFalse(after.mustTakeCard)
        assertEquals(cardsBefore + 1, after.human.cardCount)
        assertEquals(deckBefore - 1, after.deckCount)
        assertFalse("the turn passes once the card is taken", after.isHumanTurn)
    }

    @Test
    fun `taking the card says which card it was`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        playUntilCardOwed(viewModel)
        val handBefore = viewModel.playing.hand.flatMap { it.owned }.map { it.id }

        viewModel.takeCard()

        val taken = viewModel.playing.message
        assertTrue("$taken should name the card taken", taken is GameMessage.CardTaken)
        val country = (taken as GameMessage.CardTaken).country
        assertFalse("the card named has to be a new one", country.id in handBefore)
        // And it is the card that actually arrived, not just any name.
        val handAfter = viewModel.playing.hand.flatMap { it.owned }.map { it.id } +
            viewModel.playing.humanCompletedQuartets.flatMap { entry -> entry.countries.map { it.id } }
        assertTrue("${country.id} should now be held", country.id in handAfter)
        assertTrue("the record is kept", viewModel.playing.history.contains(taken))
    }

    @Test
    fun `the computer players wait until the card has been taken`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        playUntilCardOwed(viewModel)
        val waiting = viewModel.playing

        viewModel.advance()
        advanceUntilIdle()

        assertTrue("still owed a card", viewModel.playing.mustTakeCard)
        assertEquals(waiting.history, viewModel.playing.history)
        assertEquals(waiting.human.cardCount, viewModel.playing.human.cardCount)
    }

    @Test
    fun `auto play never stops to take a card`() = runTest {
        val viewModel = newViewModel(seed = 6, autoPlay = true)
        advanceUntilIdle()

        playHumanTurns(viewModel, count = 6)

        assertFalse(viewModel.playing.mustTakeCard)
    }

    @Test
    fun `turning auto on takes a card that was waiting`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        playUntilCardOwed(viewModel)

        viewModel.setAutoPlay(true)
        advanceUntilIdle()

        assertFalse(viewModel.playing.mustTakeCard)
        assertTrue(
            "play should have come back to the human",
            viewModel.playing.isHumanTurn || viewModel.playing.isFinished,
        )
    }

    @Test
    fun `play again clears a card that was waiting to be taken`() = runTest {
        val viewModel = newViewModel(autoPlay = false)
        advanceUntilIdle()
        playUntilCardOwed(viewModel)

        viewModel.startNewGame()
        advanceUntilIdle()

        assertFalse(viewModel.playing.mustTakeCard)
        assertTrue(viewModel.playing.canAct)
    }
}
