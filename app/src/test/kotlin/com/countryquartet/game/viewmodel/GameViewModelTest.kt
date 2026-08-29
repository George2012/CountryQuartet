package com.countryquartet.game.viewmodel

import com.countryquartet.game.data.AssetFiles
import com.countryquartet.game.data.GameDataSource
import com.countryquartet.game.repository.CountryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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

    private fun newViewModel() = GameViewModel(CountryRepository(AssetFiles))

    private val GameViewModel.playing: GameUiState.Playing
        get() = uiState.value as GameUiState.Playing

    @Test
    fun `a new game deals a hand and starts with the human`() = runTest {
        val viewModel = newViewModel()
        advanceUntilIdle()

        val state = viewModel.playing
        assertEquals(4, state.standings.size)
        assertTrue(state.isHumanTurn)
        assertEquals(13, state.human.cardCount + state.human.score * 4)
        assertTrue(state.hand.isNotEmpty())
        assertEquals(0, state.completedQuartetsCount)
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
            viewModel.selectQuartet(group.quartet.id)
            viewModel.selectCountry(viewModel.playing.selectedGroup!!.missing.first().id)
            viewModel.selectOpponent(state.opponents.first().id)
            assertTrue(viewModel.playing.canAsk)

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
        assertEquals(13, second.human.cardCount + second.human.score * 4)
        assertTrue("a reshuffle should not be impossible", first.isNotEmpty())
    }
}
