package com.countryquartet.game.data

import com.countryquartet.game.model.Country
import com.countryquartet.game.model.Quartet

/**
 * Synthetic 52 card / 13 quartet dataset used to test the validator: every test
 * starts from a valid dataset and breaks exactly one rule.
 */
object TestDataset {

    fun countries(): List<Country> = quartets().flatMap { quartet ->
        quartet.countryIds.map { id -> country(id, quartet.id) }
    }

    fun quartets(): List<Quartet> = (0 until GameDataValidator.EXPECTED_QUARTET_COUNT).map { index ->
        Quartet(
            id = "q$index",
            name = "Quartet $index",
            countryIds = (0 until GameDataValidator.COUNTRIES_PER_QUARTET).map { "c${index}_$it" },
        )
    }

    fun country(id: String, quartetId: String, name: String = "Country $id"): Country = Country(
        id = id,
        name = name,
        quartetId = quartetId,
        capital = "Capital of $id",
        language = "Language of $id",
        currency = "Currency of $id",
        flagAsset = "flags/$id.png",
        funFact = "Fun fact about $id.",
    )
}
