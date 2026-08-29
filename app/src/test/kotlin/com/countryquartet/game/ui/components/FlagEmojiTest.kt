package com.countryquartet.game.ui.components

import com.countryquartet.game.game.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlagEmojiTest {

    @Test
    fun `a country code becomes two regional indicator symbols`() {
        assertEquals("🇸🇪", flagEmoji("se"))
        assertEquals(listOf(0x1F1F8, 0x1F1EA), flagEmoji("se").codePoints().toArray().toList())
    }

    @Test
    fun `the code may be given in any case`() {
        assertEquals(flagEmoji("se"), flagEmoji("SE"))
        assertEquals(flagEmoji("se"), flagEmoji(" Se "))
    }

    @Test
    fun `anything that is not a two letter code yields nothing`() {
        assertEquals("", flagEmoji(""))
        assertEquals("", flagEmoji("s"))
        assertEquals("", flagEmoji("swe"))
        assertEquals("", flagEmoji("s1"))
        assertEquals("", flagEmoji("--"))
    }

    @Test
    fun `every country of the dataset produces a flag`() {
        TestGame.gameData.countries.forEach { country ->
            val flag = flagEmoji(country.id)
            assertEquals(country.id, 2, flag.codePoints().toArray().size)
            assertTrue(country.id, flag.codePoints().allMatch { it in 0x1F1E6..0x1F1FF })
        }
    }
}
