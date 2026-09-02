package com.countryquartet.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A small round badge for one seat at the table: a person for the human, a
 * robot for a computer player, tinted so all four seats stay tellable apart.
 *
 * A ring lights up around it while it is that player's turn, on top of the
 * screen's own current-player highlight - this is meant to catch the eye even
 * when the rest of the row is only glanced at.
 */
@Composable
fun PlayerAvatar(
    isHuman: Boolean,
    seatIndex: Int,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    val ringColor by animateColorAsState(
        targetValue = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = Motion.spec(),
        label = "avatarRing",
    )
    Box(
        modifier = modifier
            .size(AVATAR_SIZE)
            .border(width = 3.dp, color = ringColor, shape = CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .background(seatColor(seatIndex)),
        contentAlignment = Alignment.Center,
    ) {
        val tint = MaterialTheme.colorScheme.onSurface
        val glyphModifier = Modifier.size(AVATAR_SIZE * 0.6f)
        if (isHuman) PersonGlyph(tint, glyphModifier) else RobotGlyph(tint, glyphModifier)
    }
}

/** One of four theme-aware colours, so every seat reads as a different player at a glance. */
@Composable
private fun seatColor(seatIndex: Int): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.primaryContainer,
    )
    return colors[seatIndex % colors.size]
}

private val AVATAR_SIZE = 40.dp

// The two glyphs below are deliberately simple shapes rather than an icon
// library: at 40dp they only need to read as "a person" or "a robot" next to
// each other, not stand alone as art.

@Composable
private fun PersonGlyph(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(color = tint, radius = w * 0.2f, center = Offset(w / 2f, h * 0.28f))
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.12f, h * 0.48f),
            size = Size(w * 0.76f, h * 0.62f),
        )
    }
}

@Composable
private fun RobotGlyph(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawLine(
            color = tint,
            start = Offset(w / 2f, h * 0.02f),
            end = Offset(w / 2f, h * 0.16f),
            strokeWidth = w * 0.07f,
        )
        drawCircle(color = tint, radius = w * 0.06f, center = Offset(w / 2f, h * 0.05f))
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f, h * 0.16f),
            size = Size(w * 0.64f, h * 0.42f),
            cornerRadius = CornerRadius(w * 0.12f),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.26f, h * 0.64f),
            size = Size(w * 0.48f, h * 0.3f),
            cornerRadius = CornerRadius(w * 0.08f),
        )
    }
}
