package com.countryquartet.game.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.countryquartet.game.R
import com.countryquartet.game.ui.components.PlaceholderContent
import com.countryquartet.game.ui.components.ScreenScaffold
import com.countryquartet.game.ui.theme.CountryQuartetTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = stringResource(R.string.title_settings),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        PlaceholderContent(
            message = stringResource(R.string.placeholder_settings),
            innerPadding = innerPadding,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CountryQuartetTheme {
        SettingsScreen(onBack = {})
    }
}
