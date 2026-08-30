package com.countryquartet.game.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countryquartet.game.R
import com.countryquartet.game.ui.theme.DeckCountGreen

/**
 * The face down draw pile with the number of cards left written on it.
 *
 * Cards behind the top one are hinted at with a small offset, so the pile
 * visibly thins out as the game goes on, and the exact number is on the card
 * because the pile running out changes the rules.
 */
@Composable
fun DeckPile(
    count: Int,
    modifier: Modifier = Modifier,
    width: Dp = 44.dp,
) {
    val height = width * 88 / 64
    val label = stringResource(R.string.deck_cards_left, count)

    Column(
        modifier = modifier.semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (count > 0) {
                // Two hints of a stack, then the card the number sits on.
                if (count > 8) {
                    DeckCard(width, height, Modifier.offset(x = 4.dp, y = (-4).dp).alpha(0.5f))
                }
                if (count > 2) {
                    DeckCard(width, height, Modifier.offset(x = 2.dp, y = (-2).dp).alpha(0.75f))
                }
                DeckCard(width, height, Modifier)
                Text(
                    text = count.toString(),
                    color = DeckCountGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = width, height = height)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(6.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        // Same green as a full deck, darkened so it still reads
                        // on the light empty frame.
                        text = "0",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.deck_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeckCard(width: Dp, height: Dp, modifier: Modifier) {
    Image(
        painter = painterResource(R.drawable.card_back),
        contentDescription = null,
        modifier = modifier.size(width = width, height = height),
    )
}
