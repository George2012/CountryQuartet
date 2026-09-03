package com.countryquartet.game.ui.screens

import com.countryquartet.game.game.TestGame
import com.countryquartet.game.viewmodel.GameMessage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The green/red rule for status and history messages.
 *
 * Worth pinning down: "good" is judged from the human's seat, not the acting
 * player's, and the two read opposite for half of these. An inverted branch
 * would still compile and still colour every message - just the wrong one.
 */
class MessageToneTest {

    private val quartet = TestGame.gameData.quartets.first()
    private val countries = TestGame.gameData.countriesOf(quartet.id)

    @Test
    fun `an ask that works is good only for the asker`() {
        assertTrue(received(askerIsHuman = true).isGoodForHuman())
        assertFalse(received(askerIsHuman = false).isGoodForHuman())
        assertTrue(regionPresent(askerIsHuman = true).isGoodForHuman())
        assertFalse(regionPresent(askerIsHuman = false).isGoodForHuman())
    }

    @Test
    fun `an ask that fails is good for everyone except the asker`() {
        // Including two computers: a rival losing its turn is good news from
        // the human's seat, even though no card of theirs moved.
        assertFalse(refused(askerIsHuman = true).isGoodForHuman())
        assertTrue(refused(askerIsHuman = false).isGoodForHuman())
        assertFalse(regionAbsent(askerIsHuman = true).isGoodForHuman())
        assertTrue(regionAbsent(askerIsHuman = false).isGoodForHuman())
    }

    @Test
    fun `a completed quartet is good only for the player who completed it`() {
        assertTrue(completed(askerIsHuman = true).isGoodForHuman())
        assertFalse(completed(askerIsHuman = false).isGoodForHuman())
    }

    @Test
    fun `the card a lost turn pays out is good news`() {
        assertTrue(GameMessage.CardTaken(countries.first()).isGoodForHuman())
    }

    private fun received(askerIsHuman: Boolean) = GameMessage.CardReceived(
        askerName = "Asker",
        targetName = "Target",
        countryName = countries.first().name,
        askerIsHuman = askerIsHuman,
        targetIsHuman = false,
    )

    private fun refused(askerIsHuman: Boolean) = GameMessage.CardRefused(
        askerName = "Asker",
        targetName = "Target",
        countryName = countries.first().name,
        askerIsHuman = askerIsHuman,
        targetIsHuman = false,
    )

    private fun regionPresent(askerIsHuman: Boolean) = GameMessage.RegionPresent(
        askerName = "Asker",
        targetName = "Target",
        quartetName = quartet.name,
        askerIsHuman = askerIsHuman,
        targetIsHuman = false,
    )

    private fun regionAbsent(askerIsHuman: Boolean) = GameMessage.RegionAbsent(
        askerName = "Asker",
        targetName = "Target",
        quartetName = quartet.name,
        askerIsHuman = askerIsHuman,
        targetIsHuman = false,
    )

    private fun completed(askerIsHuman: Boolean) = GameMessage.QuartetCompleted(
        playerName = "Asker",
        targetName = "Target",
        countryName = countries.first().name,
        askerIsHuman = askerIsHuman,
        targetIsHuman = false,
        quartet = quartet,
        countries = countries,
    )
}
