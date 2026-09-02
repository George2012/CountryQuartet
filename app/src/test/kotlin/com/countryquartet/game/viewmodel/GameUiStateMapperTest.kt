package com.countryquartet.game.viewmodel

import com.countryquartet.game.game.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameUiStateMapperTest {

    private val engine = TestGame.engine
    private val gameData = TestGame.gameData

    private fun map(
        state: com.countryquartet.game.model.GameState,
        selection: Selection = Selection(),
        message: GameMessage? = null,
        history: List<GameMessage> = emptyList(),
    ) = playingState(engine, gameData, state, selection, message, history)

    @Test
    fun `the hand is grouped by quartet with owned and missing countries`() {
        val state = TestGame.stateOf(
            listOf("se", "no", "it"),
            listOf("dk"),
            listOf("fi"),
            listOf("es"),
        )

        val groups = map(state).hand

        assertEquals(listOf("nordic_countries", "southern_europe"), groups.map { it.quartet.id })
        assertEquals(listOf("Sweden", "Norway"), groups[0].owned.map { it.name })
        assertEquals(listOf("Denmark", "Finland"), groups[0].missing.map { it.name })
        assertEquals(listOf("Italy"), groups[1].owned.map { it.name })
    }

    @Test
    fun `quartets closest to completion are listed first`() {
        val state = TestGame.stateOf(
            listOf("it", "se", "no", "dk"),
            listOf("fi"),
            listOf("es"),
            listOf("jp"),
        )

        assertEquals(
            listOf("nordic_countries", "southern_europe"),
            map(state).hand.map { it.quartet.id },
        )
    }

    @Test
    fun `standings show scores, card counts and whose turn it is`() {
        val state = TestGame.stateOf(
            listOf("se", "no"),
            listOf("dk"),
            listOf("fi"),
            listOf("es"),
            currentPlayerIndex = 2,
            completed = listOf(listOf("east_asia"), emptyList(), emptyList(), emptyList()),
        )

        val standings = map(state).standings

        assertEquals(listOf(1, 0, 0, 0), standings.map { it.score })
        assertEquals(listOf(2, 1, 1, 1), standings.map { it.cardCount })
        assertEquals(listOf(false, false, true, false), standings.map { it.isCurrent })
        assertTrue(standings.first().isHuman)
    }

    @Test
    fun `asking the region is only possible with a legal quartet and opponent picked, not yet confirmed`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("dk"), listOf("fi"))

        assertFalse(map(state).canAskRegion)
        assertFalse(map(state, Selection(quartetId = "nordic_countries")).canAskRegion)
        assertFalse(map(state, Selection(opponentId = "p1")).canAskRegion)
        assertTrue(
            map(state, Selection(quartetId = "nordic_countries", opponentId = "p1")).canAskRegion,
        )
        // Already confirmed this turn: nothing left to ask about the region.
        assertFalse(
            map(
                state,
                Selection(
                    quartetId = "nordic_countries",
                    opponentId = "p1",
                    confirmedRegions = setOf("p1" to "nordic_countries"),
                ),
            ).canAskRegion,
        )
    }

    @Test
    fun `asking the country is only possible once its region is confirmed`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("dk"), listOf("fi"))
        val confirmed = setOf("p1" to "nordic_countries")

        assertFalse(map(state).canAsk)
        assertFalse(map(state, Selection(countryId = "no", opponentId = "p1")).canAsk)
        assertTrue(
            map(state, Selection(countryId = "no", opponentId = "p1", confirmedRegions = confirmed)).canAsk,
        )
    }

    @Test
    fun `an illegal pick does not enable asking`() {
        val state = TestGame.stateOf(listOf("se"), listOf("jp"), listOf("dk"), listOf("fi"))

        // Japan belongs to a quartet the human holds no card of.
        assertFalse(map(state, Selection(quartetId = "east_asia", opponentId = "p1")).canAskRegion)
        assertFalse(
            map(
                state,
                Selection(countryId = "jp", opponentId = "p1", confirmedRegions = setOf("p1" to "east_asia")),
            ).canAsk,
        )
    }

    @Test
    fun `the history is exposed as given, most recent entry first`() {
        val state = TestGame.stateOf(listOf("se"), listOf("no"), listOf("dk"), listOf("fi"))
        val history = listOf(
            GameMessage.CardReceived("Player 0", "Player 1", "Norway", askerIsHuman = true, targetIsHuman = false),
            GameMessage.CardRefused(
                "Player 0", "Player 2", "Denmark", askerIsHuman = true, targetIsHuman = false,
            ),
        )

        assertEquals(history, map(state, history = history).history)
    }

    @Test
    fun `it is not the human turn while a computer player moves`() {
        val state = TestGame.stateOf(
            listOf("se"), listOf("no"), listOf("dk"), listOf("fi"),
            currentPlayerIndex = 1,
        )

        val playing = map(state, Selection(countryId = "no", opponentId = "p1"))

        assertFalse(playing.isHumanTurn)
        assertFalse(playing.canAsk)
        assertEquals("Player 1", playing.currentPlayerName)
    }

    @Test
    fun `completed quartets of the human are exposed`() {
        val state = TestGame.stateOf(
            listOf("se"), listOf("no"), listOf("dk"), listOf("fi"),
            completed = listOf(listOf("east_asia"), emptyList(), emptyList(), emptyList()),
        )

        assertEquals(listOf("East Asia"), map(state).humanCompletedQuartets.map { it.quartet.name })
        assertEquals(
            listOf("Japan", "South Korea", "China", "Mongolia"),
            map(state).humanCompletedQuartets.single().countries.map { it.name },
        )
        assertEquals(1, map(state).completedQuartetsCount)
        assertEquals(13, map(state).totalQuartets)
    }

    @Test
    fun `a finished game reports its winners`() {
        val finished = com.countryquartet.game.game.GameRunner.playToEnd(
            engine,
            engine.newGame(random = kotlin.random.Random(8)),
        )

        val playing = map(finished)

        assertTrue(playing.isFinished)
        assertFalse(playing.isHumanTurn)
        assertEquals(finished.winnerIds.size, playing.winnerNames.size)
        assertEquals(playing.isDraw, finished.winnerIds.size > 1)
    }
}
