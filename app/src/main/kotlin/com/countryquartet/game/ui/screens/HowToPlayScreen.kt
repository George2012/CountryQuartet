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
fun HowToPlayScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = stringResource(R.string.title_how_to_play),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        PlaceholderContent(
            message = stringResource(R.string.placeholder_how_to_play),
            innerPadding = innerPadding,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HowToPlayScreenPreview() {
    CountryQuartetTheme {
        HowToPlayScreen(onBack = {})
    }
}
