package com.countryquartet.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.countryquartet.game.R
import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Quartet
import com.countryquartet.game.ui.theme.quartetBackground

/**
 * A quartet a player has finished: the four flags stay together and the set is
 * shown as one scoring unit rather than four separate cards.
 */
@Composable
fun CompletedQuartetCard(
    quartet: Quartet,
    countries: List<Country>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = quartetBackground(quartet.id),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = quartet.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.card_completed_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                countries.forEach { country ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CountryFlag(
                            countryId = country.id,
                            size = 22.dp,
                        )
                        Text(text = country.name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
