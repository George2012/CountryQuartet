package com.countryquartet.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countryquartet.game.R
import com.countryquartet.game.ui.components.CardState
import com.countryquartet.game.ui.components.CompactCountryCard
import com.countryquartet.game.ui.components.CountryCard
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.quartetBackground
import com.countryquartet.game.viewmodel.CountriesUiState
import com.countryquartet.game.viewmodel.CountriesViewModel
import com.countryquartet.game.viewmodel.QuartetEntry

@Composable
fun CountriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CountriesViewModel = viewModel(factory = CountriesViewModel.factory(LocalContext.current)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = stringResource(R.string.title_countries),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        when (val state = uiState) {
            CountriesUiState.Loading -> CenteredText(stringResource(R.string.game_loading), innerPadding)
            is CountriesUiState.Failed -> CenteredText(
                stringResource(R.string.game_failed, state.message),
                innerPadding,
            )
            is CountriesUiState.Loaded -> CountriesList(state.quartets, innerPadding)
        }
    }
}

@Composable
private fun CenteredText(text: String, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CountriesList(quartets: List<QuartetEntry>, innerPadding: PaddingValues) {
    var openCountryId by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.countries_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(quartets, key = { it.quartet.id }) { entry ->
            // Same region colour as the game hand, so a quartet is recognisable
            // in both places.
            Surface(
                color = quartetBackground(entry.quartet.id),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(10.dp),
            ) {
                Text(
                    text = entry.quartet.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    entry.countries.forEach { country ->
                        CompactCountryCard(
                            countryId = country.id,
                            name = country.name,
                            capital = country.capital,
                            state = if (country.id == openCountryId) {
                                CardState.Selected
                            } else {
                                CardState.Normal
                            },
                            onClick = { openCountryId = country.id.takeIf { it != openCountryId } },
                        )
                    }
                }
                entry.countries.firstOrNull { it.id == openCountryId }?.let { country ->
                    CountryCard(
                        country = country,
                        quartet = entry.quartet,
                        quartetCountries = entry.countries,
                        state = CardState.Selected,
                    )
                }
            }
            }
        }
    }
}
