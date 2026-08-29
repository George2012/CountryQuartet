package com.countryquartet.game.data

import com.countryquartet.game.model.Country
import com.countryquartet.game.model.GameData
import com.countryquartet.game.model.Quartet

/**
 * Checks that a dataset really describes a playable deck before the rest of the
 * app is allowed to see it.
 *
 * Pure Kotlin: no Android and no JSON, so a future content pack can be checked
 * the same way regardless of where it was loaded from.
 */
object GameDataValidator {

    const val EXPECTED_COUNTRY_COUNT = 52
    const val EXPECTED_QUARTET_COUNT = 13
    const val COUNTRIES_PER_QUARTET = 4

    /**
     * Validates the dataset and returns it as [GameData].
     *
     * @throws GameDataException if any rule is broken; the message lists every
     *   problem found, not just the first one.
     */
    fun requireValid(countries: List<Country>, quartets: List<Quartet>): GameData {
        val problems = validate(countries, quartets)
        if (problems.isNotEmpty()) {
            throw GameDataException(
                "Invalid game data:" + problems.joinToString(separator = "") { "\n - $it" },
            )
        }
        return GameData(countries = countries, quartets = quartets)
    }

    /** Every rule violation found in the dataset, empty when the dataset is valid. */
    fun validate(countries: List<Country>, quartets: List<Quartet>): List<String> {
        val problems = mutableListOf<String>()

        if (countries.size != EXPECTED_COUNTRY_COUNT) {
            problems += "expected $EXPECTED_COUNTRY_COUNT countries but found ${countries.size}"
        }
        if (quartets.size != EXPECTED_QUARTET_COUNT) {
            problems += "expected $EXPECTED_QUARTET_COUNT quartets but found ${quartets.size}"
        }

        duplicatesOf(countries.map { it.id }).forEach { problems += "duplicate country id: $it" }
        duplicatesOf(quartets.map { it.id }).forEach { problems += "duplicate quartet id: $it" }
        duplicatesOf(countries.map { it.name }).forEach { problems += "duplicate country name: $it" }
        duplicatesOf(quartets.map { it.name }).forEach { problems += "duplicate quartet name: $it" }

        val countryIds = countries.map { it.id }.toSet()
        val quartetIds = quartets.map { it.id }.toSet()

        quartets.forEach { quartet ->
            if (quartet.countryIds.size != COUNTRIES_PER_QUARTET) {
                problems += "quartet ${quartet.id} has ${quartet.countryIds.size} countries, " +
                    "expected $COUNTRIES_PER_QUARTET"
            }
            duplicatesOf(quartet.countryIds).forEach {
                problems += "quartet ${quartet.id} lists country $it twice"
            }
            quartet.countryIds.filterNot { it in countryIds }.forEach {
                problems += "quartet ${quartet.id} references unknown country: $it"
            }
        }

        countries.forEach { country ->
            if (country.quartetId !in quartetIds) {
                problems += "country ${country.id} references unknown quartet: ${country.quartetId}"
            }
        }

        // Membership must agree in both directions: the quartet lists the country
        // and the country points back at that same quartet.
        val owningQuartets = mutableMapOf<String, MutableList<String>>()
        quartets.forEach { quartet ->
            quartet.countryIds.distinct().forEach { countryId ->
                owningQuartets.getOrPut(countryId) { mutableListOf() } += quartet.id
            }
        }
        countries.forEach { country ->
            val owners = owningQuartets[country.id].orEmpty()
            when {
                owners.isEmpty() ->
                    problems += "country ${country.id} is not part of any quartet"
                owners == listOf(country.quartetId) -> Unit
                else -> problems += "country ${country.id} claims quartet ${country.quartetId} " +
                    "but is listed by ${owners.joinToString()}"
            }
        }

        return problems
    }

    private fun duplicatesOf(values: List<String>): List<String> =
        values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.sorted()
}
