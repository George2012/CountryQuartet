package com.countryquartet.game.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.countryquartet.game.data.AssetGameDataSource
import com.countryquartet.game.model.Country
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.Quartet
import com.countryquartet.game.repository.CountryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One quartet with its four countries, ready to browse. */
data class QuartetEntry(
    val quartet: Quartet,
    val countries: List<Country>,
)

/** What the countries screen shows. */
sealed interface CountriesUiState {
    data object Loading : CountriesUiState
    data class Failed(val message: String) : CountriesUiState
    data class Loaded(val quartets: List<QuartetEntry>) : CountriesUiState
}

/** Groups the dataset for browsing. Pure, so it is covered by plain JVM tests. */
internal fun countriesUiState(gameData: GameData): CountriesUiState.Loaded =
    CountriesUiState.Loaded(
        quartets = gameData.quartets.map { quartet ->
            QuartetEntry(quartet = quartet, countries = gameData.countriesOf(quartet.id))
        },
    )

/** Backs the educational countries browser. */
class CountriesViewModel(repository: CountryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CountriesUiState>(CountriesUiState.Loading)
    val uiState: StateFlow<CountriesUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = try {
            countriesUiState(repository.gameData())
        } catch (e: Exception) {
            CountriesUiState.Failed(e.message ?: e.toString())
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    CountriesViewModel(CountryRepository(AssetGameDataSource(appContext)))
                }
            }
        }
    }
}
