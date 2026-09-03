package com.countryquartet.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

// Fixed rather than theme colours: a cartoon face has to read the same way on
// every one of the four badge tints, in both light and dark themes.
private val SKIN = Color(0xFFF5D0AC)
private val CLOTHING = Color(0xFF37474F)
private val DARK_HAIR = Color(0xFF2E2A27)
private val BROWN_HAIR = Color(0xFF6D4C33)
private val GREY_HAIR = Color(0xFF9E9E9E)
private val WHITE_HAIR = Color(0xFFF5F5F5)
private val GLASSES = Color(0xFF37474F)

/**
 * The cartoon faces of the physicists who play as the computer opponents.
 *
 * Every face is the same head and shoulders wearing one or two features - hair,
 * beard, glasses. At this size no likeness would survive, so each physicist is
 * given the one shape they are recognised by: Einstein's hair, Newton's wig,
 * Curie's bun, Planck's spectacles.
 *
 * The head is drawn in a skin tone under the features rather than everything in
 * one colour, because a single colour turns hair, face and beard into one
 * unreadable blob at 40dp.
 *
 * Shapes rather than pictures, so they cost nothing to ship, scale to any
 * screen and stay in one style.
 */
enum class PhysicistFace(internal val hair: Color) {
    EINSTEIN(WHITE_HAIR),
    NEWTON(BROWN_HAIR),
    CURIE(DARK_HAIR),
    GALILEO(GREY_HAIR),
    TESLA(DARK_HAIR),
    BOHR(BROWN_HAIR),
    FARADAY(GREY_HAIR),
    MAXWELL(DARK_HAIR),
    PLANCK(GREY_HAIR),
    KEPLER(DARK_HAIR),
    ;

    companion object {
        /**
         * The face for a player id, or null for anyone not on the roster - the
         * human, and any opponent from a game dealt before this roster existed.
         */
        fun forPlayerId(playerId: String): PhysicistFace? = when (playerId) {
            "einstein" -> EINSTEIN
            "newton" -> NEWTON
            "curie" -> CURIE
            "galileo" -> GALILEO
            "tesla" -> TESLA
            "bohr" -> BOHR
            "faraday" -> FARADAY
            "maxwell" -> MAXWELL
            "planck" -> PLANCK
            "kepler" -> KEPLER
            else -> null
        }
    }
}

@Composable
internal fun PhysicistGlyph(face: PhysicistFace, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        headAndShoulders()
        val hair = face.hair
        when (face) {
            PhysicistFace.EINSTEIN -> einstein(hair)
            PhysicistFace.NEWTON -> newton(hair)
            PhysicistFace.CURIE -> curie(hair)
            PhysicistFace.GALILEO -> galileo(hair)
            PhysicistFace.TESLA -> tesla(hair)
            PhysicistFace.BOHR -> bohr(hair)
            PhysicistFace.FARADAY -> faraday(hair)
            PhysicistFace.MAXWELL -> maxwell(hair)
            PhysicistFace.PLANCK -> planck(hair)
            PhysicistFace.KEPLER -> kepler(hair)
        }
    }
}

/** A plain face with no features: the human player's own seat. */
internal fun DrawScope.plainFace() {
    headAndShoulders()
    cap(DARK_HAIR, top = 0.16f, height = 0.22f, width = 0.44f)
}

/** The body every face is built on: shoulders in clothing, head in skin. */
private fun DrawScope.headAndShoulders() {
    val w = size.width
    val h = size.height
    drawArc(
        color = CLOTHING,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * 0.12f, h * 0.66f),
        size = Size(w * 0.76f, h * 0.5f),
    )
    drawCircle(color = SKIN, radius = w * 0.23f, center = Offset(w / 2f, h * 0.40f))
}

/** A dome of hair sitting on the top of the head. */
private fun DrawScope.cap(color: Color, top: Float, height: Float, width: Float) {
    val w = size.width
    val h = size.height
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * (0.5f - width / 2f), h * top),
        size = Size(w * width, h * height),
    )
}

/** Hair thrown out sideways, and the mustache under it. */
private fun DrawScope.einstein(hair: Color) {
    val w = size.width
    val h = size.height
    cap(hair, top = 0.15f, height = 0.20f, width = 0.40f)
    listOf(
        Triple(0.18f, 0.30f, 0.115f),
        Triple(0.82f, 0.30f, 0.115f),
        Triple(0.27f, 0.17f, 0.085f),
        Triple(0.73f, 0.17f, 0.085f),
    ).forEach { (x, y, r) ->
        drawCircle(color = hair, radius = w * r, center = Offset(w * x, h * y))
    }
    mustache(hair)
}

/** The long wig, falling past the jaw on both sides. */
private fun DrawScope.newton(hair: Color) {
    val w = size.width
    val h = size.height
    listOf(0.23f, 0.77f).forEach { x ->
        drawRoundRect(
            color = hair,
            topLeft = Offset(w * (x - 0.11f), h * 0.24f),
            size = Size(w * 0.22f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.11f),
        )
    }
    cap(hair, top = 0.16f, height = 0.24f, width = 0.48f)
}

/** Hair gathered up into a bun. */
private fun DrawScope.curie(hair: Color) {
    val w = size.width
    val h = size.height
    drawCircle(color = hair, radius = w * 0.10f, center = Offset(w / 2f, h * 0.13f))
    cap(hair, top = 0.17f, height = 0.24f, width = 0.48f)
}

/** Bald on top, full beard below. */
private fun DrawScope.galileo(hair: Color) {
    val w = size.width
    val h = size.height
    beard(hair, top = 0.42f, height = 0.36f, width = 0.42f)
    // Only a fringe of hair left, low at the sides.
    listOf(0.29f, 0.71f).forEach { x ->
        drawCircle(color = hair, radius = w * 0.07f, center = Offset(w * x, h * 0.28f))
    }
}

/** Flat hair with a centre parting, and a narrow mustache. */
private fun DrawScope.tesla(hair: Color) {
    val w = size.width
    val h = size.height
    // Two halves with a gap between them: the parting. Drawing the hair whole
    // and cutting the gap out would punch through the badge behind.
    listOf(0.28f, 0.53f).forEach { left ->
        drawArc(
            color = hair,
            startAngle = if (left < 0.5f) 180f else 270f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(w * (left - if (left < 0.5f) 0f else 0.25f), h * 0.17f),
            size = Size(w * 0.44f, h * 0.24f),
        )
    }
    mustache(hair)
}

/** A high forehead, with hair only around the sides. */
private fun DrawScope.bohr(hair: Color) {
    val w = size.width
    val h = size.height
    listOf(0.27f, 0.73f).forEach { x ->
        drawRoundRect(
            color = hair,
            topLeft = Offset(w * (x - 0.075f), h * 0.24f),
            size = Size(w * 0.15f, h * 0.24f),
            cornerRadius = CornerRadius(w * 0.075f),
        )
    }
}

/** A rounded crown with waves at the sides. */
private fun DrawScope.faraday(hair: Color) {
    val w = size.width
    val h = size.height
    listOf(0.24f, 0.76f).forEach { x ->
        drawCircle(color = hair, radius = w * 0.085f, center = Offset(w * x, h * 0.34f))
    }
    cap(hair, top = 0.15f, height = 0.26f, width = 0.50f)
}

/** A broad beard over the jaw, hair parted above. */
private fun DrawScope.maxwell(hair: Color) {
    beard(hair, top = 0.44f, height = 0.34f, width = 0.44f)
    cap(hair, top = 0.16f, height = 0.22f, width = 0.46f)
}

/** Bald, with round spectacles and a mustache. */
private fun DrawScope.planck(hair: Color) {
    val w = size.width
    val h = size.height
    listOf(0.415f, 0.585f).forEach { x ->
        drawCircle(
            color = GLASSES,
            radius = w * 0.075f,
            center = Offset(w * x, h * 0.38f),
            style = Stroke(width = w * 0.032f),
        )
    }
    drawLine(
        color = GLASSES,
        start = Offset(w * 0.49f, h * 0.38f),
        end = Offset(w * 0.51f, h * 0.38f),
        strokeWidth = w * 0.03f,
    )
    // Only a rim of hair is left round the back of the head.
    listOf(0.29f, 0.71f).forEach { x ->
        drawCircle(color = hair, radius = w * 0.065f, center = Offset(w * x, h * 0.27f))
    }
    mustache(hair)
}

/** Curls at the sides and a beard that comes to a point. */
private fun DrawScope.kepler(hair: Color) {
    val w = size.width
    val h = size.height
    drawPath(
        path = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.64f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.78f)
            close()
        },
        color = hair,
    )
    listOf(0.24f, 0.76f).forEach { x ->
        drawCircle(color = hair, radius = w * 0.085f, center = Offset(w * x, h * 0.31f))
    }
    cap(hair, top = 0.16f, height = 0.24f, width = 0.46f)
}

/** A rounded mass of beard hanging off the jaw. */
private fun DrawScope.beard(color: Color, top: Float, height: Float, width: Float) {
    val w = size.width
    val h = size.height
    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(w * (0.5f - width / 2f), h * top),
        size = Size(w * width, h * height),
    )
}

/** The bar of a mustache, worn by the faces that had one. */
private fun DrawScope.mustache(color: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.39f, h * 0.46f),
        size = Size(w * 0.22f, h * 0.075f),
        cornerRadius = CornerRadius(w * 0.037f),
    )
}

