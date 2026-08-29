package com.countryquartet.game.data

/**
 * Supplies the raw JSON of the game content.
 *
 * Keeping this behind an interface lets the game engine and its tests run on a
 * plain JVM, and leaves room for additional country packs later.
 */
interface GameDataSource {
    fun readCountriesJson(): String
    fun readQuartetsJson(): String
}
