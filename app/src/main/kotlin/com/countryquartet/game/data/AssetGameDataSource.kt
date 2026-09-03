package com.countryquartet.game.data

import android.content.Context
import java.io.IOException

/**
 * Reads the bundled dataset from the APK assets.
 *
 * This is the only place in the data layer that touches Android APIs.
 */
class AssetGameDataSource(context: Context) : GameDataSource {

    private val assets = context.applicationContext.assets

    override fun readCountriesJson(): String = read(COUNTRIES_ASSET)

    override fun readQuartetsJson(): String = read(QUARTETS_ASSET)

    override fun readPhysicistsJson(): String = read(PHYSICISTS_ASSET)

    private fun read(name: String): String = try {
        assets.open(name).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        throw GameDataException("Unable to read asset \"$name\"", e)
    }

    companion object {
        const val COUNTRIES_ASSET = "countries.json"
        const val QUARTETS_ASSET = "quartets.json"
        const val PHYSICISTS_ASSET = "physicists.json"
    }
}
