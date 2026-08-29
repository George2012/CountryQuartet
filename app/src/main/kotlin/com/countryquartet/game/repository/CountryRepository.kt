package com.countryquartet.game.repository

import com.countryquartet.game.data.GameDataParser
import com.countryquartet.game.data.GameDataSource
import com.countryquartet.game.data.GameDataValidator
import com.countryquartet.game.model.Country
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.Quartet

/**
 * Single entry point to the game content for the rest of the app.
 *
 * The dataset is read, parsed and validated once per instance and then kept in
 * memory: it is small, static and needed by every screen.
 */
class CountryRepository(private val source: GameDataSource) {

    @Volatile
    private var cached: GameData? = null

    /**
     * The validated dataset.
     *
     * @throws com.countryquartet.game.data.GameDataException if the bundled
     *   content is missing, malformed or breaks a dataset rule.
     */
    fun gameData(): GameData = cached ?: synchronized(this) {
        cached ?: load().also { cached = it }
    }

    fun countries(): List<Country> = gameData().countries

    fun quartets(): List<Quartet> = gameData().quartets

    fun country(id: String): Country = gameData().country(id)

    fun quartet(id: String): Quartet = gameData().quartet(id)

    fun countriesOf(quartetId: String): List<Country> = gameData().countriesOf(quartetId)

    private fun load(): GameData = GameDataValidator.requireValid(
        countries = GameDataParser.parseCountries(source.readCountriesJson()),
        quartets = GameDataParser.parseQuartets(source.readQuartetsJson()),
    )
}
