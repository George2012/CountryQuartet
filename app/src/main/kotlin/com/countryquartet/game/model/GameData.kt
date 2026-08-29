package com.countryquartet.game.model

/**
 * The full content of the game: every quartet and every country.
 *
 * Instances are created by the data layer only after validation, so the lookups
 * below may assume the dataset is complete and consistent. Unknown identifiers
 * therefore indicate a programming error and fail fast.
 */
data class GameData(
    val countries: List<Country>,
    val quartets: List<Quartet>,
) {
    private val countriesById: Map<String, Country> = countries.associateBy { it.id }
    private val quartetsById: Map<String, Quartet> = quartets.associateBy { it.id }

    /** All country identifiers, i.e. the complete deck. */
    val countryIds: List<String> = countries.map { it.id }

    fun country(id: String): Country =
        requireNotNull(countriesById[id]) { "Unknown country id: $id" }

    fun quartet(id: String): Quartet =
        requireNotNull(quartetsById[id]) { "Unknown quartet id: $id" }

    fun countryOrNull(id: String): Country? = countriesById[id]

    fun quartetOrNull(id: String): Quartet? = quartetsById[id]

    /** The four countries of [quartetId], in the order the quartet lists them. */
    fun countriesOf(quartetId: String): List<Country> =
        quartet(quartetId).countryIds.map(::country)

    /** The quartet the given country belongs to. */
    fun quartetOf(countryId: String): Quartet = quartet(country(countryId).quartetId)
}
