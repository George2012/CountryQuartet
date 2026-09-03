package com.countryquartet.game.data

import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Physicist
import com.countryquartet.game.model.Quartet
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Turns the raw asset JSON into models.
 *
 * The parser only checks that the JSON is structurally sound and that every
 * field is present; dataset rules such as "13 quartets of 4 countries" are the
 * job of [GameDataValidator].
 *
 * `org.json` ships with Android, so this adds no runtime dependency. JVM unit
 * tests get the same API from the `org.json:json` test dependency.
 */
object GameDataParser {

    fun parseCountries(json: String): List<Country> = parseArray(json, "country") { entry ->
        Country(
            id = entry.requireString("id", "country"),
            name = entry.requireString("name", "country"),
            quartetId = entry.requireString("quartetId", "country"),
            capital = entry.requireString("capital", "country"),
            language = entry.requireString("language", "country"),
            currency = entry.requireString("currency", "country"),
            flagAsset = entry.requireString("flagAsset", "country"),
            funFact = entry.requireString("funFact", "country"),
        )
    }

    fun parseQuartets(json: String): List<Quartet> = parseArray(json, "quartet") { entry ->
        Quartet(
            id = entry.requireString("id", "quartet"),
            name = entry.requireString("name", "quartet"),
            countryIds = entry.requireStringList("countryIds", "quartet"),
        )
    }

    fun parsePhysicists(json: String): List<Physicist> = parseArray(json, "physicist") { entry ->
        Physicist(
            id = entry.requireString("id", "physicist"),
            name = entry.requireString("name", "physicist"),
            shortName = entry.requireString("shortName", "physicist"),
        )
    }

    private fun <T> parseArray(json: String, entity: String, item: (JSONObject) -> T): List<T> {
        val array = try {
            JSONArray(json)
        } catch (e: JSONException) {
            throw GameDataException("The $entity data is not a JSON array", e)
        }
        return (0 until array.length()).map { index ->
            val entry = array.optJSONObject(index)
                ?: throw GameDataException("The $entity at index $index is not a JSON object")
            item(entry)
        }
    }

    private fun JSONObject.requireString(field: String, entity: String): String {
        val value = optString(field).trim()
        if (value.isEmpty()) {
            throw GameDataException("Missing or blank \"$field\" in $entity ${describe()}")
        }
        return value
    }

    private fun JSONObject.requireStringList(field: String, entity: String): List<String> {
        val array = optJSONArray(field)
            ?: throw GameDataException("Missing \"$field\" array in $entity ${describe()}")
        return (0 until array.length()).map { index ->
            val value = array.optString(index).trim()
            if (value.isEmpty()) {
                throw GameDataException(
                    "Blank entry $index of \"$field\" in $entity ${describe()}",
                )
            }
            value
        }
    }

    /** Short hint for error messages: the entry id when it is available. */
    private fun JSONObject.describe(): String = optString("id").ifEmpty { "<no id>" }
}
