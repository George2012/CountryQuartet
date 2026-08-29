package com.countryquartet.game.data

import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Quartet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the bundled content: these tests read the real `countries.json` and
 * `quartets.json` that ship inside the app.
 */
class DatasetTest {

    private val countries: List<Country> = GameDataParser.parseCountries(AssetFiles.readCountriesJson())
    private val quartets: List<Quartet> = GameDataParser.parseQuartets(AssetFiles.readQuartetsJson())

    @Test
    fun `dataset contains exactly 52 countries`() {
        assertEquals(52, countries.size)
    }

    @Test
    fun `dataset contains exactly 13 quartets`() {
        assertEquals(13, quartets.size)
    }

    @Test
    fun `every quartet contains exactly four countries`() {
        quartets.forEach { quartet ->
            assertEquals("quartet ${quartet.id}", 4, quartet.countryIds.size)
            assertEquals("quartet ${quartet.id}", 4, quartet.countryIds.distinct().size)
        }
    }

    @Test
    fun `country ids are unique`() {
        assertEquals(countries.size, countries.map { it.id }.distinct().size)
    }

    @Test
    fun `quartet ids are unique`() {
        assertEquals(quartets.size, quartets.map { it.id }.distinct().size)
    }

    @Test
    fun `country names are unique`() {
        assertEquals(countries.size, countries.map { it.name }.distinct().size)
    }

    @Test
    fun `every country belongs to exactly one quartet`() {
        val owners = countries.associate { country ->
            country.id to quartets.filter { country.id in it.countryIds }.map { it.id }
        }
        owners.forEach { (countryId, owningQuartets) ->
            assertEquals("country $countryId", 1, owningQuartets.size)
        }
    }

    @Test
    fun `quartet membership agrees with the quartet id on the country`() {
        quartets.forEach { quartet ->
            quartet.countryIds.forEach { countryId ->
                val country = countries.single { it.id == countryId }
                assertEquals(quartet.id, country.quartetId)
            }
        }
    }

    @Test
    fun `every referenced country exists`() {
        val knownIds = countries.map { it.id }.toSet()
        val referenced = quartets.flatMap { it.countryIds }
        assertEquals(52, referenced.size)
        assertTrue(
            "unknown country ids: ${referenced - knownIds}",
            knownIds.containsAll(referenced),
        )
    }

    @Test
    fun `every country references a known quartet`() {
        val knownQuartets = quartets.map { it.id }.toSet()
        countries.forEach { country ->
            assertTrue(
                "country ${country.id} references ${country.quartetId}",
                country.quartetId in knownQuartets,
            )
        }
    }

    @Test
    fun `country ids are lowercase two letter codes`() {
        countries.forEach { country ->
            assertTrue("country id ${country.id}", country.id.matches(Regex("[a-z]{2}")))
        }
    }

    @Test
    fun `every country has a flag asset named after its id`() {
        countries.forEach { country ->
            assertEquals("flags/${country.id}.png", country.flagAsset)
        }
    }

    @Test
    fun `no country field is blank`() {
        countries.forEach { country ->
            val fields = listOf(
                country.id, country.name, country.quartetId, country.capital,
                country.language, country.currency, country.flagAsset, country.funFact,
            )
            fields.forEach { assertTrue("blank field on ${country.id}", it.isNotBlank()) }
        }
    }

    @Test
    fun `accented characters survive being loaded from the asset`() {
        // ASCII spellings were used at first out of caution about encoding.
        // This is the guard that lets the correct spellings stay.
        val brazil = countries.single { it.id == "br" }
        val austria = countries.single { it.id == "at" }

        assertEquals("Brasília", brazil.capital)
        assertTrue(brazil.capital, brazil.capital.any { it.code > 127 })
        assertTrue(austria.funFact, austria.funFact.contains("Schönbrunn"))
    }

    @Test
    fun `the shipped dataset passes validation`() {
        assertEquals(emptyList<String>(), GameDataValidator.validate(countries, quartets))
    }
}
