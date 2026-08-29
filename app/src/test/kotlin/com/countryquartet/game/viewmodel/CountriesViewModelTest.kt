package com.countryquartet.game.viewmodel

import com.countryquartet.game.data.AssetFiles
import com.countryquartet.game.data.GameDataSource
import com.countryquartet.game.game.TestGame
import com.countryquartet.game.repository.CountryRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountriesViewModelTest {

    @Test
    fun `every quartet is offered with its four countries`() {
        val state = countriesUiState(TestGame.gameData)

        assertEquals(13, state.quartets.size)
        state.quartets.forEach { entry ->
            assertEquals(entry.quartet.name, 4, entry.countries.size)
            assertEquals(entry.quartet.countryIds, entry.countries.map { it.id })
            entry.countries.forEach { assertEquals(entry.quartet.id, it.quartetId) }
        }
    }

    @Test
    fun `all 52 countries are reachable`() {
        val countries = countriesUiState(TestGame.gameData).quartets.flatMap { it.countries }

        assertEquals(52, countries.size)
        assertEquals(52, countries.distinctBy { it.id }.size)
    }

    @Test
    fun `the browser loads the bundled content`() {
        val viewModel = CountriesViewModel(CountryRepository(AssetFiles))

        val state = viewModel.uiState.value
        assertTrue(state.toString(), state is CountriesUiState.Loaded)
        assertEquals(13, (state as CountriesUiState.Loaded).quartets.size)
    }

    @Test
    fun `broken content is reported instead of crashing`() {
        val broken = object : GameDataSource {
            override fun readCountriesJson(): String = "[]"
            override fun readQuartetsJson(): String = AssetFiles.readQuartetsJson()
        }

        val viewModel = CountriesViewModel(CountryRepository(broken))

        assertTrue(viewModel.uiState.value is CountriesUiState.Failed)
    }
}
