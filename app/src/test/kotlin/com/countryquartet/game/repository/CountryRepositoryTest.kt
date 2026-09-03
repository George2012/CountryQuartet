package com.countryquartet.game.repository

import com.countryquartet.game.data.AssetFiles
import com.countryquartet.game.data.GameDataException
import com.countryquartet.game.data.GameDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryRepositoryTest {

    private val repository = CountryRepository(AssetFiles)

    @Test
    fun `loads the bundled dataset`() {
        assertEquals(52, repository.countries().size)
        assertEquals(13, repository.quartets().size)
    }

    @Test
    fun `looks up a country by id`() {
        val sweden = repository.country("se")
        assertEquals("Sweden", sweden.name)
        assertEquals("Stockholm", sweden.capital)
        assertEquals("nordic_countries", sweden.quartetId)
    }

    @Test
    fun `looks up the countries of a quartet in order`() {
        val nordic = repository.countriesOf("nordic_countries")
        assertEquals(listOf("Sweden", "Norway", "Denmark", "Finland"), nordic.map { it.name })
    }

    @Test
    fun `every quartet resolves to four existing countries`() {
        repository.quartets().forEach { quartet ->
            assertEquals(quartet.id, 4, repository.countriesOf(quartet.id).size)
        }
    }

    @Test
    fun `an unknown country id fails fast`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            repository.country("atlantis")
        }
        assertTrue(error.message!!, error.message!!.contains("atlantis"))
    }

    @Test
    fun `the dataset is read only once`() {
        val counting = CountingSource(AssetFiles)
        val repository = CountryRepository(counting)

        val first = repository.gameData()
        val second = repository.gameData()

        assertSame(first, second)
        assertEquals(1, counting.reads)
    }

    @Test
    fun `invalid content is rejected`() {
        val truncated = object : GameDataSource {
            override fun readCountriesJson(): String = "[]"
            override fun readQuartetsJson(): String = AssetFiles.readQuartetsJson()
            override fun readPhysicistsJson(): String = AssetFiles.readPhysicistsJson()
        }
        val error = assertThrows(GameDataException::class.java) {
            CountryRepository(truncated).gameData()
        }
        assertTrue(error.message!!, error.message!!.contains("expected 52 countries but found 0"))
    }

    private class CountingSource(private val delegate: GameDataSource) : GameDataSource {
        var reads = 0
            private set

        override fun readCountriesJson(): String {
            reads++
            return delegate.readCountriesJson()
        }

        override fun readQuartetsJson(): String = delegate.readQuartetsJson()

        override fun readPhysicistsJson(): String = delegate.readPhysicistsJson()
    }
}
