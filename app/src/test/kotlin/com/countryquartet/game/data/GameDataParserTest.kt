package com.countryquartet.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameDataParserTest {

    @Test
    fun `parses every field of a country`() {
        val country = GameDataParser.parseCountries(
            """
            [
              {
                "id": "se",
                "name": "Sweden",
                "quartetId": "nordic_countries",
                "capital": "Stockholm",
                "language": "Swedish",
                "currency": "Swedish krona",
                "flagAsset": "flags/se.png",
                "funFact": "Sweden has a lot of islands."
              }
            ]
            """.trimIndent(),
        ).single()

        assertEquals("se", country.id)
        assertEquals("Sweden", country.name)
        assertEquals("nordic_countries", country.quartetId)
        assertEquals("Stockholm", country.capital)
        assertEquals("Swedish", country.language)
        assertEquals("Swedish krona", country.currency)
        assertEquals("flags/se.png", country.flagAsset)
        assertEquals("Sweden has a lot of islands.", country.funFact)
    }

    @Test
    fun `parses a quartet and keeps the country order`() {
        val quartet = GameDataParser.parseQuartets(
            """
            [
              {
                "id": "nordic_countries",
                "name": "Nordic Countries",
                "countryIds": ["se", "no", "dk", "fi"]
              }
            ]
            """.trimIndent(),
        ).single()

        assertEquals("nordic_countries", quartet.id)
        assertEquals("Nordic Countries", quartet.name)
        assertEquals(listOf("se", "no", "dk", "fi"), quartet.countryIds)
    }

    @Test
    fun `parses an empty array`() {
        assertEquals(emptyList<Any>(), GameDataParser.parseCountries("[]"))
        assertEquals(emptyList<Any>(), GameDataParser.parseQuartets("[]"))
    }

    @Test
    fun `rejects malformed json`() {
        val error = assertThrows(GameDataException::class.java) {
            GameDataParser.parseCountries("{ not json ")
        }
        assertTrue(error.message!!, error.message!!.contains("not a JSON array"))
    }

    @Test
    fun `rejects a missing country field`() {
        val error = assertThrows(GameDataException::class.java) {
            GameDataParser.parseCountries("""[{ "id": "se", "name": "Sweden" }]""")
        }
        assertTrue(error.message!!, error.message!!.contains("quartetId"))
        assertTrue(error.message!!, error.message!!.contains("se"))
    }

    @Test
    fun `rejects a blank country field`() {
        val json = """
            [
              {
                "id": "se",
                "name": "  ",
                "quartetId": "nordic_countries",
                "capital": "Stockholm",
                "language": "Swedish",
                "currency": "Swedish krona",
                "flagAsset": "flags/se.png",
                "funFact": "Sweden has a lot of islands."
              }
            ]
        """.trimIndent()
        val error = assertThrows(GameDataException::class.java) { GameDataParser.parseCountries(json) }
        assertTrue(error.message!!, error.message!!.contains("\"name\""))
    }

    @Test
    fun `rejects a quartet without country ids`() {
        val error = assertThrows(GameDataException::class.java) {
            GameDataParser.parseQuartets("""[{ "id": "nordic_countries", "name": "Nordic" }]""")
        }
        assertTrue(error.message!!, error.message!!.contains("countryIds"))
    }

    @Test
    fun `rejects a non object entry`() {
        val error = assertThrows(GameDataException::class.java) {
            GameDataParser.parseCountries("""["sweden"]""")
        }
        assertTrue(error.message!!, error.message!!.contains("index 0"))
    }
}
