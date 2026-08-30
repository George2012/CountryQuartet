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

    private fun newViewModel(seed: Long = 1, animations: Boolean = true) = GameViewModel(
        repository = CountryRepository(AssetFiles),
        ai = BasicAi(Random(seed)),
        random = Random(seed),
        settings = InMemorySettingsRepository(GameSettings(animationsEnabled = animations)),
    )

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
    fun `selecting a group, country and opponent enables asking`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val group = viewModel.playing.hand.first()
        viewModel.selectQuartet(group.quartet.id)
        assertEquals(group.quartet.id, viewModel.playing.selection.quartetId)
        assertFalse(viewModel.playing.canAsk)

        viewModel.selectCountry(group.missing.first().id)
        viewModel.selectOpponent(viewModel.playing.opponents.first().id)

        assertTrue(viewModel.playing.canAsk)
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
            val selected = requireNotNull(viewModel.playing.selectedGroup) {
                "group ${group.quartet.id} should be selected"
            }
            viewModel.selectCountry(selected.missing.first().id)
            // Selecting is a toggle, and a successful ask keeps the opponent
            // picked, so tapping the same one again would clear it.
            val opponent = state.opponents.first().id
            if (viewModel.playing.selection.opponentId != opponent) {
                viewModel.selectOpponent(opponent)
            }
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
        while (!viewModel.playing.isFinished && moves++ < 500) {
            val state = viewModel.playing
            val group = state.hand.first()
            if (state.selection.quartetId != group.quartet.id) {
                viewModel.selectQuartet(group.quartet.id)
            }
            viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
            // Rotate through the opponents; always asking the same one can go a
            // whole game without a single hit. Skip when already selected,
            // because selecting is a toggle.
            val opponent = state.opponents[moves % state.opponents.size].id
            if (viewModel.playing.selection.opponentId != opponent) {
                viewModel.selectOpponent(opponent)
            }

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

    /** Plays [count] human turns, letting the computer players finish each time. */
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
            viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
            val opponent = state.opponents[move % state.opponents.size].id
            if (viewModel.playing.selection.opponentId != opponent) {
                viewModel.selectOpponent(opponent)
            }
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
        )
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
        )
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
}
